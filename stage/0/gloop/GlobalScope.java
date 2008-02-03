// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007, 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.HashMap;
import java.util.Vector;

import java.io.IOException;
import static gloop.Tokenizer.*;

public class GlobalScope extends Scope {
   final public static String rcsid = "$Header$";

   public GlobalScope () {

      new Ent ("let") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            //             /* Type tp = */ p.opttype (sc);
            String sym = p.sym ();
            p.chk (ASGN);
            p.pexpr (c, sc);
            c.put ("deflocal", sym);
            sc.putDef (sym);
            return Tokenizer.empty_tokens;
         }
         public String desc () { return "<let>"; }
      };

      new Ent ("var") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            //             /* Type tp = */ p.opttype (sc);
            String sym = p.sym ();
            p.chk (ASGN);
            p.pexpr (c, sc);
            c.put ("varlocal", sym);
            sc.putVar (sym);
            return Tokenizer.empty_tokens;
         }
         public String desc () { return "<var>"; }
      };

      new Ent ("print") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            p.pexpr (c, sc);
            c.put ("print");
            return Tokenizer.empty_tokens;
         }
         public String desc () { return "<print>"; }
      };

      abstract class FunEnt extends Ent {
         String fn;
         protected FunEnt (String n) {
            super (n);
            fn = n;
         }
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            Code nc = new Code (c);
            Scope ns = new Scope (sc);
            p.chk (LPAR);
            if (!p.is (RPAR)) {
               while (true) {
                  String n = p.sym ();
                  nc.put ("arg", n);
                  // Allow fun (var a) to override the def?
                  ns.putDef (n);
                  if (!p.is (COMMA)) {
                     break;
                  }
               }
               p.chk (RPAR);
            }
            nc.put ("begin");
            p.chk (LBRC);
            p.parse (nc, ns, RBRC);
            nc.put ("ret");
            c.put (fn, nc.finish ());
            return Tokenizer.empty_tokens;
         }
      }

      new FunEnt ("fun") {
         public String desc () { return "<fun>"; }
      };

      new FunEnt ("meth") {
         public String desc () { return "<meth>"; }
      };
   }
}
