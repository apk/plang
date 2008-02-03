// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007, 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.Vector;

final public class Tokenizer {
   final public static String rcsid = "$Header$";

   // This is identified by identity so it does not matter that
   // stupid people may modify the actual vector.
   final public static Vector<Tokenizer.Token> empty_tokens =
      new Vector<Tokenizer.Token> ();

   final public static String
      EOF = "<eof>",
      // TYP = "<typ>",
      NUM = "<num>",
      ASGN = "=",
      SEMI = ";",
      COMMA = ",",
      LPAR = "(",
      RPAR = ")",
      LBRC = "{",
      RBRC = "}",
      AST = "*",
      STR = "<str>",
      SYM = "<sym>";

   public static class Token {
      public String tok;
      public String val;
      public int line;
      public Token (String t, String v, int l) {
         tok = t;
         val = v;
         line = l;
         System.out.println ("Token(" + tok + ":" + val + ")");
      }
      public Token (String t, int l) {
         tok = t;
         line = l;
         System.out.println ("Token(" + tok + ")");
      }
      public boolean is (String t) {
         return tok == t;
      }

      public String toString () {
         if (val != null) {
            return tok + "(" + val + ")";
         } else {
            return tok;
         }
      }
   }

   public static class TokEx extends Exception {
      TokEx (String s) {
         super (s);
      }
   }

   private final Reader rd;
   private int c;
   int line;

   public Tokenizer (Reader r) throws IOException {
      line = 1;
      rd = r;
      getc ();
   }

   private final int getc () throws IOException {
      if (c == '\n') line ++;
      c = rd.read ();
      // System.out.println ("<" + (char)c + ">");
      return c;
   }

   public void push (Vector<Token> toklist) {
      int i = 0;
      for (Token t: toklist) {
         backlist.insertElementAt (t, i ++);
      }
   }

   Vector<Token> backlist = new Vector<Token> ();

   public Token get () throws IOException, TokEx {
      if (backlist.size () > 0) {
         Token t = backlist.elementAt (0);
         backlist.removeElementAt (0);
         System.out.println ("Backlist: " + t);
         return t;
      }
      while (true) {
         ignb ();
         if (c == -1) {
            return new Token (EOF, line);
         }
         if (c == ';') {
            getc ();
            return new Token (SEMI, line);
         }
         if (c == ',') {
            getc ();
            return new Token (COMMA, line);
         }
         if (c == '(') {
            getc ();
            return new Token (LPAR, line);
         }
         if (c == ')') {
            getc ();
            return new Token (RPAR, line);
         }
         if (c == '{') {
            getc ();
            return new Token (LBRC, line);
         }
         if (c == '}') {
            getc ();
            return new Token (RBRC, line);
         }
         if (c == '*') {
            getc ();
            return new Token (AST, line);
         }
         if (c == '=') {
            getc ();
            return new Token (ASGN, line);
         }
         //          if (isup (c)) {
         //             StringBuffer coll = new StringBuffer ();
         //             while (isid (c) || isnum (c)) {
         //                coll.append ((char)c);
         //                getc ();
         //             }
         //             return new Token (TYP, coll.toString (), line);
         //          }
         if (isid (c)) {
            StringBuffer coll = new StringBuffer ();
            while (isid (c) || isnum (c)) {
               coll.append ((char)c);
               getc ();
            }
            return new Token (SYM, coll.toString (), line);
         }
         if (isnum (c)) {
            StringBuffer coll = new StringBuffer ();
            while (isnum (c)) {
               coll.append ((char)c);
               getc ();
            }
            return new Token (NUM, coll.toString (), line);
         }
         if (c == '"') {
            StringBuffer coll = new StringBuffer ();
            getc ();
            while (c != '"') {
               if (c == -1) throw new TokEx ("unterminated string");
               pchr (coll);
            }
            getc ();
            return new Token (STR, coll.toString (), line);
         }
         if (c >= ' ' && c <= '~') {
            throw new TokEx ("bad char '" + (char)c + "'");
         }
         throw new TokEx ("bad char " + c);
      }
   }

   private final void ignb () throws IOException {
      while (true) {
         if (isblnk (c)) {
            getc ();
         } else if (c == '#') {
            while (c != -1 && c != '\n' && c != '\r') getc ();
         } else {
            break;
         }
      }
   }

   private static boolean isblnk (int c) {
      return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
   }

   private static boolean isnum (int c) {
      return (c >= '0' && c <= '9');
   }

   private static int hexof (int c) {
      if (c >= 'a' && c <= 'f') return c - 'a' + 10;
      if (c >= 'A' && c <= 'F') return c - 'A' + 10;
      if (c >= '0' && c <= '9') return c - '0';
      return -1;
   }

   private static boolean isup (int c) {
      return (c >= 'A' && c <= 'Z');
   }

   private static boolean isid (int c) {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
         c == '_' || c == '$';
   }

   private static boolean isopchar (int c) {
      return c == '-' || c == '+' || c == '_' || c == '&' ||
         c == '/' || c == '*' || c == '!' || c == '@' ||
         c == '<' || c == '>' || c == '=' || c == '%' ||
         c == '$' || c == '.';
   }

   private void pchr (StringBuffer sb) throws IOException, TokEx {
      if (c != '\\') {
         sb.append ((char) c);
         getc ();
         return;
      }
      getc ();
      if (c == -1) {
         throw new TokEx ("eof in name/string");
      }
      switch (c) {
      case 'n': sb.append ('\n'); break;
      case 'r': sb.append ('\r'); break;
      case 't': sb.append ('\t'); break;
      case 'f': sb.append ('\f'); break;
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
         int v = c - '0';
         getc ();
         if (c >= '0' && c <= '9') {
            v = 8 * v + c - '0';
            getc ();
            if (c >= '0' && c <= '9') {
               v = 8 * v + c - '0';
               getc ();
            }
         }
         sb.append ((char) v);
         return;
      case 'u':
         v = 0;
         getc ();
         for (int i = 0; i < 4; i++) {
            int z = hexof (c);
            if (z >= 0) {
               v = 16 * v + z - '0';
            } else {
               break;
            }
            getc ();
         }
         sb.append ((char) v);
         return;
      default:
         sb.append ((char) c);
         break;
      }
   }
}
