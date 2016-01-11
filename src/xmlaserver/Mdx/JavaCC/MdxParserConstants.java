/* Generated By:JavaCC: Do not edit this line. MdxParserConstants.java */
package xmlaserver.Mdx.JavaCC;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface MdxParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int AND = 1;
  /** RegularExpression Id. */
  int AS = 2;
  /** RegularExpression Id. */
  int AXIS = 3;
  /** RegularExpression Id. */
  int CASE = 4;
  /** RegularExpression Id. */
  int CAST = 5;
  /** RegularExpression Id. */
  int CELL = 6;
  /** RegularExpression Id. */
  int CHAPTERS = 7;
  /** RegularExpression Id. */
  int COLUMNS = 8;
  /** RegularExpression Id. */
  int DIMENSION = 9;
  /** RegularExpression Id. */
  int DRILLTHROUGH = 10;
  /** RegularExpression Id. */
  int ELSE = 11;
  /** RegularExpression Id. */
  int EMPTY = 12;
  /** RegularExpression Id. */
  int END = 13;
  /** RegularExpression Id. */
  int EXPLAIN = 14;
  /** RegularExpression Id. */
  int FIRSTROWSET = 15;
  /** RegularExpression Id. */
  int FOR = 16;
  /** RegularExpression Id. */
  int FROM = 17;
  /** RegularExpression Id. */
  int IN = 18;
  /** RegularExpression Id. */
  int IS = 19;
  /** RegularExpression Id. */
  int MATCHES = 20;
  /** RegularExpression Id. */
  int MAXROWS = 21;
  /** RegularExpression Id. */
  int MEMBER = 22;
  /** RegularExpression Id. */
  int NON = 23;
  /** RegularExpression Id. */
  int NOT = 24;
  /** RegularExpression Id. */
  int NULL = 25;
  /** RegularExpression Id. */
  int ON = 26;
  /** RegularExpression Id. */
  int OR = 27;
  /** RegularExpression Id. */
  int PAGES = 28;
  /** RegularExpression Id. */
  int PLAN = 29;
  /** RegularExpression Id. */
  int PROPERTIES = 30;
  /** RegularExpression Id. */
  int RETURN = 31;
  /** RegularExpression Id. */
  int ROWS = 32;
  /** RegularExpression Id. */
  int SECTIONS = 33;
  /** RegularExpression Id. */
  int SELECT = 34;
  /** RegularExpression Id. */
  int SET = 35;
  /** RegularExpression Id. */
  int THEN = 36;
  /** RegularExpression Id. */
  int WHEN = 37;
  /** RegularExpression Id. */
  int WHERE = 38;
  /** RegularExpression Id. */
  int XOR = 39;
  /** RegularExpression Id. */
  int WITH = 40;
  /** RegularExpression Id. */
  int CROSSJOIN = 41;
  /** RegularExpression Id. */
  int DRILLDOWNLEVEL = 42;
  /** RegularExpression Id. */
  int DRILLDOWNMEMBER = 43;
  /** RegularExpression Id. */
  int HIERARCHIZE = 44;
  /** RegularExpression Id. */
  int MEMBERS = 45;
  /** RegularExpression Id. */
  int CELL_PROPERTY = 46;
  /** RegularExpression Id. */
  int DIMENTION_PROPERTY = 47;
  /** RegularExpression Id. */
  int ASTERISK = 53;
  /** RegularExpression Id. */
  int BANG = 54;
  /** RegularExpression Id. */
  int COLON = 55;
  /** RegularExpression Id. */
  int COMMA = 56;
  /** RegularExpression Id. */
  int CONCAT = 57;
  /** RegularExpression Id. */
  int DOT = 58;
  /** RegularExpression Id. */
  int EQ = 59;
  /** RegularExpression Id. */
  int GE = 60;
  /** RegularExpression Id. */
  int GT = 61;
  /** RegularExpression Id. */
  int LBRACE = 62;
  /** RegularExpression Id. */
  int LE = 63;
  /** RegularExpression Id. */
  int LPAREN = 64;
  /** RegularExpression Id. */
  int LT = 65;
  /** RegularExpression Id. */
  int MINUS = 66;
  /** RegularExpression Id. */
  int NE = 67;
  /** RegularExpression Id. */
  int PLUS = 68;
  /** RegularExpression Id. */
  int RBRACE = 69;
  /** RegularExpression Id. */
  int RPAREN = 70;
  /** RegularExpression Id. */
  int SOLIDUS = 71;
  /** RegularExpression Id. */
  int START_QUOTED_ID = 72;
  /** RegularExpression Id. */
  int END_QUOTED_ID = 73;
  /** RegularExpression Id. */
  int QUOTED_ID_CHAR = 74;
  /** RegularExpression Id. */
  int ID = 75;
  /** RegularExpression Id. */
  int AMP_UNQUOTED_ID = 76;
  /** RegularExpression Id. */
  int LETTER = 77;
  /** RegularExpression Id. */
  int DIGIT = 78;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int QUOTED_ID_STATE = 1;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"AND\"",
    "\"AS\"",
    "\"AXIS\"",
    "\"CASE\"",
    "\"CAST\"",
    "\"CELL\"",
    "\"CHAPTERS\"",
    "\"COLUMNS\"",
    "\"DIMENSION\"",
    "\"DRILLTHROUGH\"",
    "\"ELSE\"",
    "\"EMPTY\"",
    "\"END\"",
    "\"EXPLAIN\"",
    "\"FIRSTROWSET\"",
    "\"FOR\"",
    "\"FROM\"",
    "\"IN\"",
    "\"IS\"",
    "\"MATCHES\"",
    "\"MAXROWS\"",
    "\"MEMBER\"",
    "\"NON\"",
    "\"NOT\"",
    "\"NULL\"",
    "\"ON\"",
    "\"OR\"",
    "\"PAGES\"",
    "\"PLAN\"",
    "\"PROPERTIES\"",
    "\"RETURN\"",
    "\"ROWS\"",
    "\"SECTIONS\"",
    "\"SELECT\"",
    "\"SET\"",
    "\"THEN\"",
    "\"WHEN\"",
    "\"WHERE\"",
    "\"XOR\"",
    "\"WITH\"",
    "\"CROSSJOIN\"",
    "\"DRILLDOWNLEVEL\"",
    "\"DRILLDOWNMEMBER\"",
    "\"HIERARCHIZE\"",
    "\"MEMBERS\"",
    "<CELL_PROPERTY>",
    "<DIMENTION_PROPERTY>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\"*\"",
    "\"!\"",
    "\":\"",
    "\",\"",
    "\"||\"",
    "\".\"",
    "\"=\"",
    "\">=\"",
    "\">\"",
    "\"{\"",
    "\"<=\"",
    "\"(\"",
    "\"<\"",
    "\"-\"",
    "\"<>\"",
    "\"+\"",
    "\"}\"",
    "\")\"",
    "\"/\"",
    "\"[\"",
    "\"]\"",
    "<QUOTED_ID_CHAR>",
    "<ID>",
    "<AMP_UNQUOTED_ID>",
    "<LETTER>",
    "<DIGIT>",
  };

}
