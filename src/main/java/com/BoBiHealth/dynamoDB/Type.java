package com.BoBiHealth.dynamoDB;

public enum Type {
	Int{
		@Override
		public String rawValue(){
			return "Int";
		}
	},Int32{
		@Override
		public String rawValue(){
			return "Int32";
		}
	},Bool{
		@Override
		public String rawValue(){
			return "Bool";
		}
	},Str{
		@Override
		public String rawValue(){
			return "Str";
		}
	},Str_Set{
		@Override
		public String rawValue(){
			return "Str_Set";
		}
	},List{
		@Override
		public String rawValue(){
			return "List";
		}
	},Double{
		@Override
		public String rawValue(){
			return "Double";
		}
	},Map{
		@Override
		public String rawValue(){
			return "Map";
		}
	};
	public abstract String rawValue();

}
