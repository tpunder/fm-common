/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.common

import java.lang.annotation.Annotation
import java.lang.reflect.Modifier
import java.net.URLDecoder
import java.io.File
import java.util.jar.{JarFile, JarEntry}
import scala.collection.JavaConverters._

object ClassUtil extends Logging {

  def classExists(cls: String, classLoader: ClassLoader = currentThreadClassLoader): Boolean = try {
    classLoader.loadClass(cls)
    true
  } catch {
    case _: ClassNotFoundException => false
  }
  
  def requireClass(cls: String, msg: => String, classLoader: ClassLoader = currentThreadClassLoader): Unit = {
    if (!classExists(cls, classLoader)) throw new ClassNotFoundException(s"Missing Class: $cls - $msg")
  }
  
  def findAnnotatedClasses[T <: Annotation](basePackage: String, annotationClass: Class[T], classLoader: ClassLoader = currentThreadClassLoader): Set[Class[_]] = {
    findClassNames(basePackage, classLoader).filterNot { _.contains("$") }.map{ classLoader.loadClass }.filter { c: Class[_] =>
      c.getAnnotation(annotationClass) != null
    }
  }

  def findImplementingClasses[T](basePackage: String, clazz: Class[T], classLoader: ClassLoader = currentThreadClassLoader): Set[Class[_ <: T]] = {    
    findClassNames(basePackage, classLoader).filterNot { _.contains("$") }.map{ classLoader.loadClass }.filter { c: Class[_] =>
      clazz.isAssignableFrom(c)
    }.filterNot{ c: Class[_] => 
      val mods: Int = c.getModifiers()
      Modifier.isAbstract(mods) || Modifier.isInterface(mods)
    }.map{ _.asInstanceOf[Class[_ <: T]] }
  }
  
  /**
   * Find all class names under the base package (includes anonymous/inner/objects etc...)
   */
  private def findClassNames(basePackage: String, classLoader: ClassLoader = currentThreadClassLoader): Set[String] = {
    findClasspathFiles(basePackage, classLoader).filter{ f: File =>
      f.getName.endsWith(".class")
    }.map{ f: File =>
      val name: String = f.toString()
      name.substring(0, name.length - ".class".length).replace(File.separator, ".")
    }
  }
  
  private def findClasspathFiles(basePackage: String, classLoader: ClassLoader = currentThreadClassLoader): Set[File] = {
    val packageDirPath: String = basePackage.replace(".", File.separator)
    val urls: Set[URL] = classLoader.getResources(packageDirPath).asScala.toSet
    
    urls.flatMap { url: URL =>
      url.getProtocol() match {
        case "jar" => 
          val fullJarUrl: String = URLDecoder.decode(url.getFile(), "UTF-8")
          val jarFile: String = fullJarUrl.substring("file:".length, fullJarUrl.indexOf("!"))
          val jarPrefix: String = fullJarUrl.substring(fullJarUrl.indexOf("!") + 1)
          require(jarPrefix == File.separator+packageDirPath, s"Expected jarPrefix ($jarPrefix) to equal package prefix ($packageDirPath)")
          scanJar(packageDirPath+File.separator, new File(jarFile))
          
        case "file" => 
          val packageDir: File = new File(url.getFile())
          if (packageDir.isDirectory) recursiveListFiles(packageDir).map{ f: File => packageDir.toPath.relativize(f.toPath).toFile }.map{ f: File => new File(packageDirPath, f.toString) } else Nil
        
        case _ => 
          logger.warn("Unknown classpath entry: "+url)
          Nil
      }
    }
  }
  
  private def recursiveListFiles(dir: File): Set[File] = {
    require(dir.isDirectory, s"Expected file to be a directory: $dir")
    dir.listFiles.flatMap { f: File =>
      if (f.isFile) List(f)
      else if (f.isDirectory) recursiveListFiles(f)
      else Nil
    }.toSet
  }
  
  private def scanJar(prefix: String, jarFile: File): Set[File] = {
    require(jarFile.isFile, s"Missing jar file: $jarFile")
    if (prefix != "") {
      require(!prefix.startsWith(File.separator), "Prefix should not starts with /")
      require(prefix.endsWith(File.separator), "Non-Empty prefix should end with /")
    }

    val builder = Set.newBuilder[File]
    
    Resource.using(new JarFile(jarFile)){ jar: JarFile => 
      jar.entries().asScala.foreach { entry: JarEntry =>
        val name: String = entry.getName
        if (!entry.isDirectory && name.startsWith(prefix)) builder += new File(name)
      }
    }
    
    builder.result
  }

  private def currentThreadClassLoader: ClassLoader = Thread.currentThread.getContextClassLoader
}
