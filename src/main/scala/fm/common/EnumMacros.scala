/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 by Lloyd Chan
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * This is from: https://github.com/lloydmeta/enumeratum
 */
package fm.common

import scala.reflect.macros.Context
import scala.util.control.NonFatal

// TODO switch to blackbox.Context when dropping support for 2.10.x

object EnumMacros {

  def findValuesImpl[A: c.WeakTypeTag](c: Context): c.Expr[IndexedSeq[A]] = {
    import c.universe._
    val resultType = implicitly[c.WeakTypeTag[A]].tpe
    val typeSymbol = weakTypeOf[A].typeSymbol
    validateType(c)(typeSymbol)
    val subclassSymbols = enclosedSubClasses(c)(typeSymbol)
    if (subclassSymbols.isEmpty) {
      c.Expr[IndexedSeq[A]](reify(IndexedSeq.empty[A]).tree)
    } else {
      c.Expr[IndexedSeq[A]](
        Apply(
          TypeApply(
            Select(reify(IndexedSeq).tree, newTermName("apply")),
            List(TypeTree(resultType))
          ),
          subclassSymbols.map(Ident(_)).toList
        )
      )
    }
  }

  private[this] def validateType(c: Context)(typeSymbol: c.universe.Symbol): Unit = {
    if (!typeSymbol.asClass.isSealed)
      c.abort(
        c.enclosingPosition,
        "You can only use findValues on sealed traits or classes"
      )
  }

  private[this] def enclosedSubClasses(c: Context)(typeSymbol: c.universe.Symbol): Seq[c.universe.Symbol] = {
    import c.universe._
    val enclosingBodySubclasses: List[Symbol] = try {
      /*
        When moving beyond 2.11, we should use this instead, because enclosingClass will be deprecated.

        val enclosingModuleMembers = c.internal.enclosingOwner.owner.typeSignature.decls.toList
        enclosingModuleMembers.filter { x =>
          try (x.asModule.moduleClass.asClass.baseClasses.contains(typeSymbol)) catch { case _: Throwable => false }
        }

        Unfortunately, 2.10.x does not support .enclosingOwner :P
      */
      val enclosingModule = c.enclosingClass match {
        case md @ ModuleDef(_, _, _) => md
        case _ => c.abort(
          c.enclosingPosition,
          "The enum (i.e. the class containing the case objects and the call to `findValues`) must be an object"
        )
      }
      enclosingModule.impl.body.filter { x =>
        try {
          Option(x.symbol) match {
            case Some(sym) if sym.isModule => sym.asModule.moduleClass.asClass.baseClasses.contains(typeSymbol)
            case _ => false
          }
        } catch {
          case NonFatal(e) =>
            c.warning(c.enclosingPosition, s"Got an exception, indicating a possible bug in Enumeratum. Message: ${e.getMessage}")
            false
        }
      }.map(_.symbol)
    } catch { case NonFatal(e) => c.abort(c.enclosingPosition, s"Unexpected error: ${e.getMessage}") }
    if (!enclosingBodySubclasses.forall(x => x.isModule))
      c.abort(c.enclosingPosition, "All subclasses must be objects.")
    else enclosingBodySubclasses
  }

}
