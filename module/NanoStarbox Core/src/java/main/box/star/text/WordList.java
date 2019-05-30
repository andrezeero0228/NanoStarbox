package box.star.text;

import box.star.state.RuntimeObjectMapping;
import box.star.text.basic.Scanner;

public class WordList implements RuntimeObjectMapping.ObjectWithLabel {
  final String label;
  final String[] words;
  public WordList(String label, String... words){
    this.label = label;
    this.words = new String[words.length];
    System.arraycopy(words, 0, words, 0, words.length);
    Scanner.preventWordListShortCircuit(this.words);
  }
  @Override
  public String getRuntimeLabel() {
    return label;
  }
  @Override
  public String toString() {
    return getRuntimeLabel();
  }
  public boolean contains(String string){
    for (String word:words) if (word.equals(string)) return true;
    return false;
  }
  public boolean containsIgnoreCase(String string){
    for(String word:words) if (word.equalsIgnoreCase(string))return true;
    return false;
  }
}
