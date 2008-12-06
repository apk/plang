// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007, 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.HashMap;
import java.util.Vector;

import java.io.IOException;
import static gloop.Tokenizer.*;

public class GlobalScope extends Scope {

   public GlobalScope () {

      new Ent ("let") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c,
                                                 LocalScope sc)
            throws IOException, Tokenizer.TokEx
         {
            //             /* Type tp = */ p.opttype (sc);
            String sym = p.sym ();
            p.chk (ASGN);
            p.pexpr (c, sc);
            LocalScope.FrameEnt e = sc.putDef (sym);
            c.put ("lstore", e.getOffset ());
            return Tokenizer.empty_tokens;
         }
         public String desc () { return "<let>"; }
      };

      new Ent ("var") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c,
                                                 LocalScope sc)
            throws IOException, Tokenizer.TokEx
         {
            //             /* Type tp = */ p.opttype (sc);
            String sym = p.sym ();
            p.chk (ASGN);
            p.pexpr (c, sc);
            LocalScope.FrameEnt e = sc.putVar (sym);
            c.put ("lstore", e.getOffset ());
            return Tokenizer.empty_tokens;
         }
         public String desc () { return "<var>"; }
      };

      new Ent ("print") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c,
                                                 LocalScope sc)
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
         public Vector<Tokenizer.Token> macstmt (Parser p, Code c,
                                                 LocalScope sc)
            throws IOException, Tokenizer.TokEx
         {
            Code nc = new Code (c);
            LocalScope ns = new LocalScope (sc);
            int argc = 0;
            p.chk (LPAR);
            if (!p.is (RPAR)) {
               while (true) {
                  String n = p.sym ();
                  // Allow fun (var a) to override the def?
                  argc ++;
                  ns.putDef (n);
                  if (!p.is (COMMA)) {
                     break;
                  }
               }
               p.chk (RPAR);
            }
            p.chk (LBRC);
            p.parse (nc, ns, RBRC);
            nc.put ("ret");
            c.put ("numval", nc.finish ());
            c.put (fn, argc);
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
