/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scalax.io.resource

import scalax.io._
import scalax.io.resource._
import Path.AccessModes._
import scalax.test.sugar._

import org.junit.Assert._
import org.junit.{
  Test, Before, After, Rule, Ignore
}
import org.junit.rules.TemporaryFolder
import util.Random

import java.io.{
    IOException, StringReader, InputStreamReader, BufferedReader
}

class CharInputStreamTest extends AssertionSugar with IOSugar{
    implicit val codec = Codec("UTF-8")
 
    val sample ="a1?£©àäカ゚ゼ"

    @Test
    def stream_converts_chars_to_bytes() : Unit = {
        System.out.println(sample)
        System.out.println("expecting "+new BufferedReader(new StringReader(sample)).readLine)
        val reader = new StringReader(sample)
        val in = new CharInputStream(Left(reader))(codec)
        
        
        val in2 = new BufferedReader(new InputStreamReader(in, codec.name))
        val read = in2.readLine
        System.out.println("'%s' - '%s'".format(sample, read))
        assertArrayEquals(sample.toArray, read.toArray)
    }
}