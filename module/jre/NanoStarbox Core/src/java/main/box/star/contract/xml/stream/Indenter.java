/*
 * Indenter.java July 2006
 *
 * Copyright (C) 2006, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package box.star.contract.xml.stream;

/**
 * The <code>Indenter</code> is used create indent strings using the
 * stack paradigm. This allows XML documents to be generated by 
 * pushing and popping indents onto the stack. This indenter caches
 * all indent strings created so that when the same position on the
 * stack is encountered the indent can be acquired quickly.
 * <p>
 * The indents created by this are all prefixed with the line feed
 * character, which allows XML tags to span exclusive lines. If the
 * indent size specified is zero or less then no spaces, or line 
 * feed will be added to the generated indent string.
 *
 * @author Niall Gallagher
 */ 
class Indenter {

   /**
    * Provides a quick string cache that caches using by index.
    */         
   private Cache cache;
       
   /**
    * Number of spaces that is used for each of the indents.
    */ 
   private int indent;
   
   /**
    * Represents the current number of spaces in the indent text.
    */ 
   private int count;

   /**
    * Represents the index within the cache to get the indent.
    */ 
   private int index;
  
   /**
    * Constructor for the <code>Indenter</code> object. This will
    * create an indent that uses three spaces for each indent that
    * is pushed on to the stack. This also uses a default cache 
    * size of sixteen, which should be sufficient for most files.
    */ 
   public Indenter() {
      this(new Format());
   }

   /**
    * Constructor for the <code>Indenter</code> object. This will
    * create an indent that uses the specified number of spaces to
    * create each entry pushed on to the stack. This uses a cache
    * size of sixteen, which should be sufficient for most files.
    *
    * @param format determines the number of spaces per indent
    */ 
   public Indenter(Format format) {
      this(format, 16);           
   }
   
   /**
    * Constructor for the <code>Indenter</code> object. This will
    * create an indent that uses the specified number of spaces to
    * create each entry pushed on to the stack. This uses a cache
    * of the specified size, which is used to optimize the object.
    *
    * @param format determines the number of spaces per indent
    * @param size this is the initial size of the indent cache
    */ 
   private Indenter(Format format, int size) {
      this.indent = format.getIndent();           
      this.cache = new Cache(size);
   }  
   
   /**
    * This returns the current indent for this indenter. This should
    * be used to write elements or comments that should be at the
    * same indentation level as the XML element that will follow. 
    * 
    * @return this returns the current indentation level for this
    */
   public String top() {
      return indent(index);
   }

   /**
    * This is used to push an indent on to the cache. The first
    * indent created by this is an empty string, this is because an
    * indent is not required for the start of an XML file. If there
    * are multiple roots written to the same writer then the start
    * and end tags of a root element will exist on the same line.
    * 
    * @return this is used to push an indent on to the stack
    */ 
   public String push() {
      String text = indent(index++);
   
      if(indent > 0) {
         count += indent;
      }         
      return text;
   }                  

   /**
    * This is used to pop an indent from the cache. This reduces
    * the length of the current indent and is typically used when
    * an end tag is added to an XML document. If the number of pop
    * requests exceeds the number of push requests then an empty
    * string is returned from this method.
    *
    * @return this is used to pop an indent from the stack
    */ 
   public String pop() {
      String text = indent(--index);

      if(indent > 0) {
         count -= indent;
      }      
      return text;
   }

   /**
    * This is used to acquire the indent at the specified index. If
    * the indent does not exist at the specified index then on is
    * created using the current value of the indent. The very first
    * indent taken from this will be an empty string value.
    *
    * @param index this is the index to acquire the indent from
    *
    * @return this returns the indent from the specified index
    */ 
   private String indent(int index) {
      if(indent > 0) {
         String text = cache.get(index);

         if(text == null){
            text = create();
            cache.set(index, text);         
         }
         if(cache.size() >0) {
            return text;
         }            
      }         
      return "";  
   }

   /**
    * This is used to create an indent which can later be pushed on
    * to the stack. If the number of spaces to be added is zero then
    * this will return a single character string with a line feed.
    *
    * @return this will create an indent to be added to the stack
    */ 
   private String create() {
      char[] text = new char[count+1];

      if(count > 0) {
         text[0] = '\n';

         for(int i = 1; i <= count; i++){
            text[i] = ' ';                 
         }         
         return new String(text);
      }
      return "\n";
   }

   /**
    * The <code>Cache</code> object is used create an indexable list
    * which allows the indenter to quickly acquire an indent using
    * a stack position. This ensures that the indenter need only 
    * create an index once for a given stack position. The number of
    * indents held within this cache can also be tracked.
    */ 
   private static class Cache {

      /**
       * This is used to track indent strings within the cache.
       */            
      private String[] list;

      /**
       * Represents the number of indent strings held by the cache.
       */ 
      private int count;
      
      /**
       * Constructor for the <code>Cache</code> object. This creates
       * a cache of the specified size, the specified size acts as
       * an initial size and the cache can be expanded on demand.
       *
       * @param size the initial number of entries in the cache
       */ 
      public Cache(int size) {
         this.list = new String[size];              
      }

      /**
       * This method is used to retrieve the number of indents that
       * have been added to the cache. This is used to determine if
       * an indent request is the first.
       *
       * @return this returns the number of indents in the cache
       */ 
      public int size() {
         return count;              
      }      

      /**
       * This method is used to add the specified indent on to the
       * cache. The index allows the cache to act as a stack, when
       * the index is specified it can be used to retrieve the same
       * indent using that index.
       * 
       * @param index this is the position to add the index to
       * @param text this is the indent to add to the position
       */ 
      public void set(int index, String text) {
         if(index >= list.length) {
            resize(index * 2);                             
         }
         if(index > count) {
            count = index;                 
         }
         list[index] = text;         
      }

      /**
       * This method is used to retrieve an indent from the given
       * position. This allows the indenter to use the cache as a
       * stack, by increasing and decreasing the index as required.
       * 
       * @param index the position to retrieve the indent from
       *
       * @return this is the indent retrieve from the given index
       */ 
      public String get(int index) {
         if(index < list.length) {
            return list[index];
         }              
         return null;
      }

      /**
       * Should the number of indents to be cache grows larger than
       * the default initial size then this will increase the size
       * of the cache. This ensures that the indenter can handle an
       * arbitrary number of indents for a given output.
       *
       * @param size this is the size to expand the cache to
       */ 
      private void resize(int size) {
         String[] temp = new String[size];

         for(int i = 0; i < list.length; i++){
            temp[i] = list[i];                 
         }
         list = temp;
      }
   }
}
