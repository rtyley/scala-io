/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, Jesse Eichar             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scalax.io

import java.io.ByteArrayInputStream
import java.nio.channels.Channels
import java.nio.ByteBuffer
import scalaio.test.stream.InputTest
import collection.mutable.ArrayBuffer
import java.io.IOException

class ByteChannelResourceTraversableTest extends ResourceTraversableTest {
    /*        
  override def traversable[U, A](tsize: Int,
                                 callback: (Int) => U,
                                 dataFunc: (Int) => Traversable[Int],
                                 conv: (Int) => A,
                                 closeFunction: () => Unit = () => (),
                                 resourceContext:ResourceContext):LongTraversable[A] = {
    def channel = Channels.newChannel(new ByteArrayInputStream(dataFunc(tsize) map {_.toByte} toArray))
    val toInt = (bb:ByteBuffer) => new scalax.io.nio.ByteBuffer(bb).map(byte => conv(byte.toInt)) : Traversable[A]
    val resource = new CloseableOpenedResource(channel, resourceContext, CloseAction[Any](_ => closeFunction()))
    ResourceTraversable.byteChannelBased(resource, resourceContext, () => None, initialConv=toInt).map{i => callback(i); i}
  } */
}

class ByteChannelResourceInputTest extends InputTest {
  def resource(sep:String) =
    Resource.fromReadableByteChannel(Channels.newChannel(stringBasedStream(sep)))
  protected override def input(t: Type):Input = t match {
    case t@TextNewLine => resource(t.sep)
    case t@TextPair => resource(t.sep)
    case t@TextCarriageReturn => resource(t.sep)
    case TextCustom(sep) => resource(sep)
    case TextCustomData(sep, data) => Resource.fromReadableByteChannel(
      Channels.newChannel(new ByteArrayInputStream(data.getBytes(Codec.UTF8.charSet)))
    )
    case ErrorOnRead => 
      Resource.fromReadableByteChannel(Channels.newChannel(ErrorOnRead.errorInputStream))
    case ErrorOnClose => 
      Resource.fromReadableByteChannel(Channels.newChannel(ErrorOnClose.errorInputStream))
    case Image =>
      Resource.fromReadableByteChannel(
        Channels.newChannel(scalaio.test.Constants.IMAGE.openStream())
      )
  }

}

class SeekableByteChannelResourceInputTest extends InputTest {
  
  def wrap(data:Array[Byte]) = Resource.fromSeekableByteChannel(
    new ArrayBufferSeekableChannel(ArrayBuffer.apply(data:_*),StandardOpenOption.ReadWrite:_*)(_=>(),_=>())
  )
  protected override def input(t: Type):Input = t match {
    case t@TextNewLine => wrap(text(t.sep))
    case t@TextPair => wrap(text(t.sep))
    case t@TextCarriageReturn => wrap(text(t.sep))
    case TextCustom(sep) => wrap(text(sep))
    case TextCustomData(sep, data) => wrap(data.getBytes(Codec.UTF8.charSet))
    case ErrorOnRead => 
      Resource.fromSeekableByteChannel(
        new ArrayBufferSeekableChannel(ArrayBuffer[Byte](),StandardOpenOption.ReadWrite:_*)() {
           override def read(dst : ByteBuffer) = throw new IOException("boom")
      })
    case ErrorOnClose => 
      val bytes = Resource.fromInputStream(scalaio.test.Constants.IMAGE.openStream()).byteArray
      Resource.fromSeekableByteChannel(
        new ArrayBufferSeekableChannel(ArrayBuffer[Byte](bytes:_*),StandardOpenOption.ReadWrite:_*)() {
           override def close() = 
             throw new IOException("boom")
      })
    case Image =>
      val bytes = Resource.fromInputStream(scalaio.test.Constants.IMAGE.openStream()).byteArray
      wrap(bytes)
  }
  override def sizeIsDefined = true
}
