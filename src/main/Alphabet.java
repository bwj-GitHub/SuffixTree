package main;

import java.util.HashMap;

public class Alphabet {
	
	public char[] chars;
	public int length;
	private HashMap<Character, Integer> charToIndex;

	public Alphabet(char[] chars) {
		this.chars = chars;
		length = chars.length;
		charToIndex = new HashMap<Character, Integer>();
		for (int i = 0; i < length; i++) {
			charToIndex.put(chars[i], i);
		}
	}

	public int getIndex(char character) {
		return charToIndex.get(character);
	}

}
