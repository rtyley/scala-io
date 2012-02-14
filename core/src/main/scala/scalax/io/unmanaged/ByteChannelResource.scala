package scalax.io
package unmanaged

import java.nio.channels.{ByteChannel, Channels}
import java.io.Reader
import scalax.io.ResourceAdapting.{ChannelWriterAdapter, ChannelReaderAdapter, ChannelOutputStreamAdapter, ChannelInputStreamAdapter}
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer

/**
 * A for accessing and using ByteChannels.  Class can be created using the [[scalax.io.Resource]] object.
 */
class ByteChannelResource[+A <: ByteChannel] (
    resource: A,
    val context:ResourceContext = DefaultResourceContext,
    closeAction: CloseAction[A] = CloseAction.Noop,
    protected val sizeFunc:() => Option[Long] = () => None)
  extends InputResource[A]
  with OutputResource[A]
  with ResourceOps[A, InputResource[A] with OutputResource[A], ByteChannelResource[A]] with UnmanagedResource {

  self => 

  override final val open:OpenedResource[A] = new UnmanagedOpenedResource(resource, unmanagedContext(context))
  override def close() = new CloseableOpenedResource(open.get, context, closeAction).close()
  override final val unmanaged = this
  override def updateContext(newContext:ResourceContext) = 
    new ByteChannelResource(resource, newContext, closeAction, sizeFunc)
  override def addCloseAction(newCloseAction: CloseAction[A]) = 
    new ByteChannelResource(resource, context, newCloseAction :+ closeAction, sizeFunc)
  
  override def inputStream = {
    def nResource = new ChannelInputStreamAdapter(resource)
    val closer = ResourceAdapting.closeAction(closeAction)
    new InputStreamResource(nResource,context, closer, sizeFunc)
  }
  override def outputStream = {
    def nResource = new ChannelOutputStreamAdapter(resource)
    val closer = ResourceAdapting.closeAction(closeAction)
    new OutputStreamResource(nResource,context, closer)
  }
  protected override def underlyingOutput = outputStream
  override def reader(implicit sourceCodec: Codec)  = {
    def nResource = new ChannelReaderAdapter(resource, sourceCodec)
    val closer = ResourceAdapting.closeAction(closeAction)
    new ReaderResource(nResource, context, closer)
  }
  override def writer(implicit sourceCodec: Codec) = {
    def nResource = new ChannelWriterAdapter(resource, sourceCodec)
    val closer = ResourceAdapting.closeAction(closeAction)
    new WriterResource(nResource, context, closer)
  }

  override def writableByteChannel = new WritableByteChannelResource(resource, context, closeAction)
  override def readableByteChannel = new ReadableByteChannelResource(resource, context, closeAction, sizeFunc)
  
  override def blocks(blockSize: Option[Int] = None): LongTraversable[ByteBlock] = 
    new traversable.ChannelBlockLongTraversable(blockSize, context, safeSizeFunc, open)
  
  override def bytesAsInts = ResourceTraversable.byteChannelBased[Byte,Int](this.open, context, safeSizeFunc, initialConv = ResourceTraversable.toIntConv)
  override def bytes = ResourceTraversable.byteChannelBased[Byte,Byte](this.open, context, safeSizeFunc)
  override def chars(implicit codec: Codec) = reader(codec).chars  // TODO optimize for byteChannel
  }
