// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007, 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

/* The toplevel is effectively the contents of
 * 'fun (args) { catch exit; $body }'
 * which is then invoked with the command line arguments.
 *
 * ; is a right-associative expression-separator/combinator;
 * 'let a = x; f (x)', oops, should create new scope by itself?
 * (Getting the scope right is relevant for call/cc and all sorts
 * of proper incarnating (which places do see the same incarnation
 * of a variable?).)
 */

/* In this implementation we do symbol lookup at runtime, because I am
 * to lazy to implement call frame management. Still we need to keep
 * a compile-time symbol table for lookup because macros (and potentially
 * other stuff) are also scoped. (Types have just been eradicated, so
 * they don't appear any more, though.)
 *
 * Interestingly we don't need to be as exact with the scoping (of let)
 * at compile time because the proper incarnation management happens
 * at runtime now. (But we really need to have a value chain, because
 * that is what lets basically degenerate to when we don't optimize.)
 *
 * The symbol lookup has one other interesting property: By default,
 * we only see the 'global' (that is: toplevel function) symbols
 * we define ourselves.
 *
 * Also, should be scope-check functions right away or later, when
 * the surrounding function is done? The latter does not really
 * work with strange macros, but we could defer other lookups?
 */

/* We assume an accumulator which holds a current value. That way we do
 * deal that much with the last pop-or-not after or before an expression.
 */

/*
 * Should it be 'let x = <expr>' on expression-level lets? Statement-level
 * lets ought to be dead anyway, but what is the proper right-hand side here?
 */

/*
 * Momentarily we forbid to use parameter names as variable names in
 * the top scope of a function, because we essentially make the
 * parameters local lets. (And let is not a call-by-need thing,
 * we're strict.)
 */

package gloop;

import java.io.Reader;
import java.io.IOException;
import java.util.Vector;

import static gloop.Tokenizer.*;

public class Parser {
   final public static String rcsid = "$Header$";

   final Tokenizer tk;
   Tokenizer.Token tok;

   public Parser (Tokenizer t) throws IOException, Tokenizer.TokEx {
      tk = t;
      tok = tk.get ();
   }

   public Token get () throws IOException, TokEx{
      return tok = tk.get ();
   }

   public void parse (Code c, LocalScope sc, String endt)
      throws IOException, TokEx
   {
      c.put ("nullval"); // Initialize acc: Just the default return value
      // Tokenizer.println ("parser to " + endt);
      while (!tok.is (endt)) {
         //          // Handle special case of stmt-macro
         //          // (XXX But let is later going to be an expr-macro!)
         //          if (tok.is (SYM)) {
         //             Scope.Ent e = sc.get (tok.val);
         //             if (e != null) {
         //                get ();
         //                Vector<Tokenizer.Token> toklist = e.macstmt (this, c, sc);
         //                if (toklist != null) {
         //                   // Ok, this is actually some kind of macro;
         //                   // either the list is empty (-> internally done),
         //                   // or it is a replacement.
         //                   tk.push (toklist);
         //                   continue;
         //                }
         //             }
         //          }
         pexpr (c, sc);
         chk (SEMI);
         // Tokenizer.println ("Parser at " + tok.tok);
      }
      chk (endt);
   }

   public void pexpr (Code c, LocalScope sc)
      throws IOException, Tokenizer.TokEx
   {
      pprim (c, sc);
      while (true) {
         if (is (AST)) {
            c.put ("push");
            pexpr (c, sc);
            c.put ("mult");
         } else {
            return;
         }
      }
   }

   public void pprim (Code c, LocalScope sc)
      throws IOException, Tokenizer.TokEx
   {
      /* Kernel primitives */
      if (tok.is (SYM)) {
         /* Macro application loop */
         while (tok.is (SYM)) {
            String s = tok.val;
            get ();
            Scope.Ent e = sc.getRec (s);
            if (e != null) {
               Vector<Tokenizer.Token> toklist = e.macstmt (this, c, sc);
               if (toklist == Tokenizer.empty_tokens) {
                  // Has been handled internally and only consumed tokens;
                  // we're done.
                  break;
               } else if (toklist != null) {
                  // Ok, this is actually some kind of macro;
                  // use the replacement and redo.
                  tk.push (toklist);
                  continue;
               } else {
                  // XXX Need to check other special cases,
                  // or load via Ent.something
                  // XXX Is it possible to make this case the other way,
                  // by calling e.makeLoadCode()? Or would that be
                  // equally ugly?
                  if (e instanceof LocalScope.FrameEnt) {
                     LocalScope.FrameEnt fe = (LocalScope.FrameEnt)e;
                     int z = sc.countTo (fe.scope ());
                     if (z == 0) {
                        c.put ("lload", fe.getOffset ());
                     } else {
                        c.put ("up", z);
                        c.put ("load", fe.getOffset ());
                     }
                  } else {
                     throw new IllegalArgumentException (
                        "internal: not a frame ent");
                  }
               }
            } else {
               throw new IllegalArgumentException ("undefined: " + s);
            }
            break;
         }
      } else if (tok.is (NUM)) {
         c.put ("numval", Integer.parseInt (tok.val));
         get ();
      } else if (tok.is (STR)) {
         c.put ("strval", tok.val);
         get ();
      } else if (is (LPAR)) {
         pexpr (c, sc);
         chk (RPAR);
      } else {
         throw new IllegalArgumentException ("in pexpr (" + tok.tok + ")");
      }

      /* Postfix loop */
      while (true) {
         if (is (LPAR)) {
            int cnt = 0;
            if (!is (RPAR)) {
               while (true) {
                  c.put ("push");
                  pexpr (c, sc);
                  c.put ("swap");
                  cnt ++;
                  if (!is (COMMA)) break;
               }
               chk (RPAR);
            }
            c.put ("call", cnt); // fn in acc, args on stack
         } else {
            break;
         }
      }
   }

   public void chk (String t) throws IOException, Tokenizer.TokEx {
      if (!tok.is (t)) {
         throw new IllegalArgumentException ("at '" + tok.tok +
                                             "': not a '" + t + "'");
      }
      get ();
   }

   public boolean is (String t) throws IOException, Tokenizer.TokEx {
      if (!tok.is (t)) {
         return false;
      }
      get ();
      return true;
   }

   public String sym () throws IOException, Tokenizer.TokEx {
      if (!tok.is (SYM)) {
         throw new IllegalArgumentException ("not a sym");
      }
      String s = tok.val;
      get ();
      return s;
   }

   //    public void opttype (Scope sc) throws IOException, Tokenizer.TokEx {
   //       if (tok.is (TYP)) {
   //          get ();
   //          // return Type;
   //       }
   //       // return null
   //    }

   public static Runner parse (Reader r) throws IOException, Tokenizer.TokEx {
      try {
         Tokenizer t = new Tokenizer (r);
         Parser p = new Parser (t);
         CodeStore cs = new CodeStore ();
         Code c = new Code (cs);
         p.parse (c, new LocalScope (new GlobalScope ()), Tokenizer.EOF);
         c.put ("stop");
         c.finish ();
         Tokenizer.flush ();
         cs.dump ();
         return cs.getProg ();
      } finally {
         Tokenizer.flush ();
      }
   } 
}
