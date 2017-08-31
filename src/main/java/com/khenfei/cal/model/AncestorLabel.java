package com.khenfei.cal.model;

public class AncestorLabel implements JSONStringEnable {

	public AncestorLabel() {
	}

	public Field ancestor() {
		return ancestor == null ? Field.EMPTY : ancestor;
	}

	public Field requestor() {
		return requestor == null ? Field.EMPTY : requestor;
	}

	public Field number() {
		return number == null ? Field.EMPTY : number;
	}

	public void setAncestor(Field ancestor) {
		this.ancestor = ancestor;
	}

	public void setRequestor(Field requestor) {
		this.requestor = requestor;
	}

	public void setNumber(Field number) {
		this.number = number;
	}

	@Override
	public String toJSON() {
		return new StringBuilder("{").append("'ancestor':" + ancestor().toJSON())
				.append(",'requestor':" + requestor().toJSON()).append(",'number':" + number().toJSON()).append("}")
				.toString();
	}

	private AncestorLabel(AncestorLabelBuilder builder) {
		setAncestor(builder.getAncestor());
		setRequestor(builder.getRequestor());
		setNumber(builder.getNumber());
	}

	private Field ancestor = null;
	private Field requestor = null;
	private Field number = null;

	public static class AncestorLabelBuilder {
		public AncestorLabelBuilder(final Integer number) {
			this.number = new Field(number.toString());
		}

		public AncestorLabelBuilder ancestor(final String name) {
			this.ancestor = new Field(name);
			return this;
		}

		public AncestorLabelBuilder requestor(final String name) {
			this.requestor = new Field(name);
			return this;
		}

		public AncestorLabel build() {
			return new AncestorLabel(this);
		}

		public Field getAncestor() {
			return ancestor;
		}

		public Field getRequestor() {
			return requestor;
		}

		public Field getNumber() {
			return number;
		}

		private Field ancestor;
		private Field requestor;
		private Field number;
	}

}
