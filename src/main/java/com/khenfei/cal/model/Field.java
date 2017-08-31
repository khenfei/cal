package com.khenfei.cal.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Field implements JSONStringEnable {
	public Field() {}
	public List<Character> value() {
		return value==null?Collections.emptyList():value;
	}
	public Field(final String name) {
		setValueWith(name);
	}	
	public void setValue(final List<Character> value) {
		this.value = (value == null)? 
				Collections.emptyList() : value; 
	}
	public void setValueWith(final String name) {
		this.value = 
				StringUtils.isBlank(name)? 
				Collections.emptyList() : 
					name.chars()
					.mapToObj(i -> (char)i)
					.collect(Collectors.toList()); 
	}
	
	@Override
	public String toJSON() {
		return new StringBuilder("{")
				.append("'value':["+value()
						.stream()
						.map(x -> x.toString())
						.collect(Collectors.joining("','", "'", "'"))+"]")
				.append("}")
				.toString();
	}
	
	private List<Character> value;
	public static final Field EMPTY = new Field("");
}
