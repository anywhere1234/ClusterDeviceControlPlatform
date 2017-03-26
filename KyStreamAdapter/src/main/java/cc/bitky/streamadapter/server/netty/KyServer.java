package cc.bitky.streamadapter.server.netty;

import cc.bitky.streamadapter.server.netty.ServerContract.IServerPresenter;
import cc.bitky.streamadapter.server.netty.ServerContract.IServerView;
import cc.bitky.streamadapter.server.netty.channelhandler.ServerChannelInitializer;
import cc.bitky.streamadapter.util.listener.SuccessfulListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;

public class KyServer implements IServerView {
  private SuccessfulListener launchListener;
  private SuccessfulListener finishListener;
  private NioEventLoopGroup group;

  public KyServer() {

  }

  @Override public void setPresenter(IServerPresenter presenter) {

  }

  @Override public void start() {
    new Thread(() -> {
      group = new NioEventLoopGroup();
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(group)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ServerChannelInitializer());
      ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(30232));
      channelFuture.addListener(
          (ChannelFutureListener) future -> startListenerHandle(future, launchListener));
    }).start();
  }

  private void startListenerHandle(Future future, SuccessfulListener listener) {
    if (!future.isSuccess()) future.cause().printStackTrace();
    if (listener != null) listener.onSuccess(future.isSuccess());
  }

  public void setLaunchSuccessfulListener(
      SuccessfulListener successfulListener) {
    this.launchListener = successfulListener;
  }

  public void setFinishSuccessfulListener(
      SuccessfulListener finishListener) {
    this.finishListener = finishListener;
  }

  @Override public void shutdown() {
    if (group != null) {
      Future<?> futureShutdown = group.shutdownGracefully();
      futureShutdown.addListener(future -> startListenerHandle(future, finishListener));
    }
  }
}
