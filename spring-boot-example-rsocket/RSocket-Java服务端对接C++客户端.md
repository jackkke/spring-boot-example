# RSocket-Java服务端对接C++客户端

## [RSocket 官网](https://rsocket.io/)

### RSocket是什么？

RSocket是一种二进制字节流传输协议，是一个应用层网络协议，并**不依赖于底层传输层**的协议，位于OSI模型中的5~6层，底层可以依赖TCP、WebSocket、Aeron协议，所以可以根据不同场景和设备选择不同的协议。

- 在数据中心中可以使用RSocket on TCP
- 在浏览器JS中可以使用RSocket on WebSocket （现在用WebAssembly也可以基于其他实现了吧）
- 要和HTTP网络中间件保持兼容可以用RSocket on HTTP/2
- 也可以基于QUIC。（从特性匹配上看QUIC很适合RSocket）

不管你怎么改变传输层协议，对于应用开发者来说都没差。基于ReactiveStream semantic的API是不会变的，用户只需要在create client/server的时候**换一个transport类**即可。

### RSocket设计目标是什么？

* 支持对象传输，包括request\response、request\stream、fire and forget、channel
* 支持应用层流量控制
* 支持单连接双向、多次复用
* 支持连接修复
* 更好的使用WebSocket和Aeron协议

## 支持语言丰富

- Java (提供 Examples)
- Kotlin (提供 Examples)
- C++ (提供 Examples)
- JavaScript
- Go
- .Net
- Swift
- Python
- Rust

## RSocket Client CLI (RSC)

[making/rsc](https://github.com/making/rsc)
前期了解试用可以使用 CLI 工具

## C++

[Github rsocket/rsocket-cpp](https://github.com/rsocket/rsocket-cpp)

[Github rsocket/rsocket-cpp Example](https://github.com/rsocket/rsocket-cpp/tree/master/rsocket/examples)

[Releases下载链接](https://github.com/rsocket/rsocket-cpp/releases/download/v2021.08.30.00/rsocket-cpp-v2021.08.30.00.tar.gz)

服务端：

```cpp
// RSocket server accepting on TCP
auto rs = RSocket::createServer(TcpConnectionAcceptor::create(FLAGS_port));
// global request handler
auto handler = std::make_shared<HelloStreamRequestHandler>();
// start accepting connections
rs->startAndPark([handler](auto r) { return handler; });                    
```

客户端：

```cpp
auto rsf = RSocket::createClient(TcpConnectionFactory::create(host, port));
auto s = std::make_shared<ExampleSubscriber>(5, 6);
auto rs = rsf->connect().get();
rs->requestStream(Payload("Jane"), s);
```

## Java 服务端

调研过程中已经搭建服务端与客户端（Java实现）

目前的服务端已经测试通过消息发送与支持断点续传的文件上传等功能，详细如下。

1. 简单字符串消息与实体消息的发送接收
   
   服务端:
   
   ```java
   @MessageMapping(RouterMapping.REQUEST_RESPONSE_STR)
   public String str(String str) {
      log.info("Received {} request: {}", RouterMapping.REQUEST_RESPONSE_STR, str);
      return "str [" + str + "] 接收成功";
   }
   
   @MessageMapping(RouterMapping.REQUEST_RESPONSE_MESSAGE)
   public Message message(Message message) {
      log.info("Received {} request: {}", RouterMapping.REQUEST_RESPONSE_MESSAGE, message);
      return new Message("SERVER", "RESPONSE");
   }
   ```
   
   客户端
   
   ```java
   @ShellMethod("简单消息发送与接收")
   public void str(String name) {
      log.info("简单消息发送与接收...");
      String message =
         rsocketRequester
            .route(RouterMapping.REQUEST_RESPONSE_STR)
            .metadata("ump-client-0006", MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
            .data("我是" + name)
            .retrieveMono(String.class)
            .block();
      log.info("返回结果: {}", message);
   }
   
   @ShellMethod("消息体发送与接收")
   public void msg(String name) {
      log.info("消息体发送与接收...");
      Message message =
         rsocketRequester
            .route(RouterMapping.REQUEST_RESPONSE_MESSAGE)
            .metadata("ump-client-0006", MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
            .data(new Message(name, name))
            .retrieveMono(Message.class)
            .block();
      log.info("返回结果: {}", message);
   }
   ```

2. 文件上传，已简单实现一版
   
   * 支持**断点续传**
     
      服务端
     
     ```java
     @MessageMapping(RouterMapping.MESSAGE_UPLOAD)
     public Flux<FileUploadVo> upload(
           @Headers Map<String, Object> metadata, @Payload Flux<DataBuffer> content) {
        FileUploadVoBuilder builder = FileUploadVo.builder();
     
        // 检查文本内容是否合法
        try {
           checkContent(content);
        } catch (Exception e) {
           return error(e);
        }
     
        // 首次上传 记录上传文件信息 -> 文件名 文件大小 文件后缀 文件原始位置
        // 自动生成一个唯一文件ID -> UUID ，并以此为文件名+文件后缀写入到磁盘
        // 断点续传 读取唯一文件ID + 继续上传位置 继续上传
        String filePath = null;
        String idName = null;
        try {
           UploadVO uploadVO = JSONObject.parseObject((String) metadata.get(UploadConstants.FILE_DATA), UploadVO.class);
           if (uploadVO.getPosition() != 0 && StringUtils.isNotBlank(uploadVO.getIdName())) {
           filePath = uploadVO.getIdName();
           idName = filePath;
           log.info("【文件续传】idName={}、position = {}", idName, uploadVO.getPosition());
           } else {
           var fileName = uploadVO.getFileName();
           var fileExt = uploadVO.getFileExt();
           filePath = fileName + "." + fileExt;
           idName = UuidUtil.getTimeBasedUuid() + "." + fileExt;
           log.info("【文件上传】fileName={}、fileExt = {}", fileName, fileExt);
           }
           var path = Paths.get(idName);
           builder.idName(idName).originalFilePath(uploadVO.getOriginalFilePath());
     
           AsynchronousFileChannel channel = createChannel(outputPath, path, StringUtils.isBlank(uploadVO.getIdName()));
           AtomicLong position = new AtomicLong(uploadVO.getPosition());
           Flux<FileUploadVo> concat = Flux.concat(
              DataBufferUtils.write(content, channel, position.get()).map(dataBuffer -> chunkCompleted(position, dataBuffer)),
              success()
           );
           return concat.doOnComplete(doOnComplete()).onErrorResume(onErrorResume(position)).doFinally(doFinally(channel));
        } catch (Exception e) {
           return error(e);
        }
     }
     
     private static final long MAX_SIZE = 2L * 1024 * 1024 * 1024;
     
     /**
        * 检查 上传文件块是否超长
        *
        * @param content 文件上传 Payload
        */
     public void checkContent(Flux<DataBuffer> content) {
        if (content == null) {
           throw new DataBufferLimitException("内容长度不可为空");
        }
        AtomicReference<Boolean> flag = new AtomicReference<>(null);
        Flux.from(content)
           .map(buffer -> {
              flag.set(Long.parseLong(String.valueOf(buffer.writePosition())) <= MAX_SIZE);
              return flag.get();
           }).subscribe();
        boolean res = BooleanUtils.isFalse(flag.get());
        if (res) {
           throw new DataBufferLimitException("内容长度超长，最大为 2G");
        }
     }
     
     public AsynchronousFileChannel createChannel(Path outputPath, Path path, boolean blank) throws IOException {
        Set<OpenOption> result = new HashSet<>(2);
        if (blank) {
           result.add(StandardOpenOption.CREATE);
        }
        result.add(StandardOpenOption.WRITE);
        return AsynchronousFileChannel.open(outputPath.resolve(path), result, null);
     }
     
     public Consumer<SignalType> doFinally(AsynchronousFileChannel channel) {
        return signalType -> {
           log.debug("FileController.doFinally signalType : {}", signalType.name());
           if (channel != null && channel.isOpen()) {
           try {
              channel.close();
           } catch (IOException ignored) {
           }
           }
        };
     }
     
     public Mono<FileUploadVo> success() {
        FileUploadVoBuilder builder = FileUploadVo.builder();
        return Mono.just(builder.message("上传成功").status(UploadStatus.COMPLETED).build());
     }
     
     public Function<Throwable, Mono<FileUploadVo>> onErrorResume(AtomicLong position) {
        return (throwable) -> {
           log.debug("FileController.onErrorResume = {}", throwable.getMessage());
           FileUploadVoBuilder builder = FileUploadVo.builder();
           return Mono.just(builder.message(throwable.getMessage()).position(position.get()).status(UploadStatus.FAILED).build());
        };
     }
     
     public Runnable doOnComplete(){
        return () -> log.debug("FileController.doOnComplete");
     }
     
     public Flux<FileUploadVo> error(Exception e) {
        FileUploadVoBuilder builder = FileUploadVo.builder();
        return Flux.just(builder.status(UploadStatus.FAILED).message(e.getMessage()).build());
     }
     
     public FileUploadVo chunkCompleted(AtomicLong position, DataBuffer dataBuffer){
        position.addAndGet(dataBuffer.writePosition());
        FileUploadVoBuilder builder = FileUploadVo.builder();
        return builder.position(position.get()).status(UploadStatus.CHUNK_COMPLETED).build();
     }
     ```
     
      客户端
     
     ```java
     @ShellMethod("Send one request. One response will be printed.")
     public void upload(@NotNull String fileName, Integer type) {
        try {
           UploadVO uploadVO;
           if (type != null && type.equals(1)) {
           FileUploadVo fileUploadVo = redisService.get(fileName, FileUploadVo.class);
           redisService.del(fileName);
           uploadVO = new UploadVO(fileUploadVo.getPosition(), fileUploadVo.getIdName(), fileUploadVo.getOriginalFilePath());
           } else {
           uploadVO = new UploadVO(uploadPath, fileName);
           }
           System.out.println("uploadVO = " + uploadVO);
           FileUrlResource fileUrlResource = new FileUrlResource(uploadVO.getOriginalFilePath());
           Flux<FileUploadVo> uploadStatusFlux = requesterMono
              .map(r -> r.route(RouterMapping.MESSAGE_UPLOAD).metadata(uploadVO::improveMeta).data(
                 DataBufferUtils.read(
                       fileUrlResource,
                       uploadVO.getPosition(),
                       new DefaultDataBufferFactory(),
                       uploadProperties.getBufferSize()
                 )
     //                  .doOnNext(s -> System.out.println("文件上传:" + s))
              ))
              .flatMapMany(r -> r.retrieveFlux(FileUploadVo.class))
              .doOnNext(this::checkResult);
           uploadStatusFlux.blockLast();
        } catch (Exception e) {
           e.printStackTrace();
        }
     }
     
     void checkResult(FileUploadVo res) {
        if (res == null) {
           log.info("上传结果为空");
        } else if (res.getStatus().equals(UploadStatus.FAILED)) {
           log.error("上传失败 {}", res);
           if (StringUtils.isNotBlank(res.getIdName())) {
           // 放到redis中
           redisService.set(res.getIdName(), res);
           }
        } else if (res.getStatus().equals(UploadStatus.COMPLETED)) {
           log.info("上传成功 \n 完整返回信息 {}", res);
        } else if (res.getStatus().equals(UploadStatus.CHUNK_COMPLETED)) {
           log.info("上传中，文件名称 {}, id {}, 当前进度为： {}", res.getOriginalFilePath(), res.getIdName(), res.getPosition());
        } else {
           log.info("上传结果为 {}", res);
        }
     }
     ```
   
   * 文件上传最大限制
   
   * 文件Chunk大小限制
   
   * 文件上传速率限制（**进行中**）

3. **集成SSL**，默认实现方案TLS
   
   * keytool生成的证书 .p12 加载到服务端，请求需要加密
     
     ```yml
     spring:
        rsocket:
           server:
              # 端口开放 建连接 实施难度
              port: 19712
              transport: TCP
              fragmentSize: 1MB
              ssl:
                 enabled: true
                 # clientAuth: NEED
                 protocol: TLS1.3
                 key-store: classpath:rsocket.p12
                 key-store-password: rsocketStore
                 keyAlias: rsocket
                 keyStoreType: PKCS12
     ```
   * 客户端自定义SslProvider -> TcpSslContextSpec-> TrustManager 加载keytool生成的客户端 .cer 证书，实现携带证书请求，或者自定义信任证书。
     
     ```java
     // 自定义sslProvider 适配自定义 trustManager
     @Bean
     public SslProvider sslProvider() {
        String keystoreFile = "rsocket.p12";
        String keystorePass = "rsocketStore";
        try {
           MyX509TrustManager myX509TrustManager = new MyX509TrustManager(keystoreFile, keystorePass);
           TcpSslContextSpec tcpSslContextSpec = TcpSslContextSpec.forClient();
           tcpSslContextSpec.configure(sslCtxBuilder ->
              sslCtxBuilder.sslProvider(OpenSsl.isAvailable() ? OPENSSL : JDK)
                 .trustManager(myX509TrustManager)
                 .ciphers(null, IdentityCipherSuiteFilter.INSTANCE)
                 .applicationProtocolConfig(null));
           return SslProvider.builder().sslContext(tcpSslContextSpec).build();
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
     }
     // 自定义X509TrustManager 加载信任证书
     class MyX509TrustManager implements X509TrustManager{
        X509TrustManager sunJSSEX509TrustManager;
        MyX509TrustManager(String keystoreFile,String pass) throws Exception {
           KeyStore ks = KeyStore.getInstance("PKCS12");
           ks.load(new ClassPathResource(keystoreFile).getInputStream(), pass.toCharArray());
           TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
           tmf.init(ks);
           TrustManager tms [] = tmf.getTrustManagers();
           for (int i = 0; i < tms.length; i++) {
           if (tms[i] instanceof X509TrustManager) {
              sunJSSEX509TrustManager = (X509TrustManager) tms[i];
              return;
           }
           }
           throw new Exception("Couldn't initialize");
        }
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
           log.debug("MyX509TrustManager.checkClientTrusted");
           sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
           log.debug("MyX509TrustManager.checkServerTrusted");
           sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
           log.debug("MyX509TrustManager.getAcceptedIssuers");
           System.out.println("MyX509TrustManager.getAcceptedIssuers");
           return sunJSSEX509TrustManager.getAcceptedIssuers();
        }
     }
     ```

4. 集成**Security**
   
   * 实现 MESSAGE_RSOCKET_AUTHENTICATION 基础加密，即客户端建立TCP连接需要setupMetadata，携带**客户端ID与客户端密码**（支持数据库配置或配置文件配置）
     
      服务端
     
     ```java
     // 增加simple auth 解析器
     @Bean
     RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
        handler.setRSocketStrategies(strategies);
        return handler;
     }
     // 加载客户端权限到内存，后续扩展为读取数据库
     @Bean
     MapReactiveUserDetailsService authentication() {
        UserDetails user = User.withDefaultPasswordEncoder()
           .username("user")
           .password("pass")
           .roles("USER")
           .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
           .username("test")
           .password("pass")
           .roles("NONE")
           .build();
        return new MapReactiveUserDetailsService(user, admin);
     }
     ```
     
      客户端
     
     ```java
     // 自定义 rsocketRequester 附加客户端信息 如id 密码等
     @Bean
     public RSocketRequester rsocketRequester(
           RSocketRequester.Builder builder, RSocketStrategies rSocketStrategies, SslProvider sslProvider) {
     
        UsernamePasswordMetadata user = new UsernamePasswordMetadata("user", "pass");
        TcpClient tcpClient = TcpClient.create()
           .host("localhost")
           .secure(sslProvider)
           .port(19712);
        String clientId = "ump-client-0001";
        return builder
           .rsocketStrategies(rSocketStrategies)
           .rsocketConnector(
                 rSocketConnector ->
                    rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2))))
           .dataMimeType(MediaType.APPLICATION_CBOR)
           .setupMetadata(user, SIMPLE_AUTH)
           .setupMetadata(clientId, MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
           .transport(TcpClientTransport.create(tcpClient));
     }
     ```
   
   * TCP **全局自定义权限控制**，目前已实现clientid校验
     
     ```java
     // TCP连接默认鉴权+自定义鉴权 或许在此扩展总连接数 与 client连接数限制等操作
     @Bean
     PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
        security.authorizePayload(authorize ->
           authorize
                 .anyExchange().access((a, ctx) -> {
                 // 处理自定义权限过滤
                 MessageHeaderAccessor headers = new MessageHeaderAccessor();
                 headers.setLeaveMutable(true);
                 MimeType mimeType = MimeTypeUtils.parseMimeType(
                       WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());
                 MetadataExtractor metadataExtractor = rSocketStrategies.metadataExtractor();
                 Map<String, Object> metadataValues = metadataExtractor.extract(ctx.getExchange().getPayload(), mimeType);
                 boolean checkClientId = clientService.checkClientId(metadataValues);
                 if (!checkClientId) {
                    log.error("tcp请求失败，请求头 client id 信息不存在或有误！");
                 }
                 log.error(" TODO 处理全局请求数限速 移至此处");
                 log.error(" TODO 处理client请求数限速 移至此处");
                 return Mono.just(new AuthorizationDecision(checkClientId));
                 })
                 .anyExchange().authenticated()
        ).simpleAuthentication(Customizer.withDefaults());
        return security.build();
     }
     ```
   
   * **TCP AOP**，目前简单实现打印日志，支持更多扩展
     
     ```java
     @Pointcut("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
     public void serviceLimit() {
     
     }
     
     @Around("serviceLimit()")
     public Object around(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Boolean flag = rateLimiter.tryAcquire();
        Object obj = null;
        try {
           if (flag) {
           log.debug("请求访问 class: {}, method: {} 成功", className, methodName);
           obj = joinPoint.proceed();
           }else{
           log.debug("请求访问 class: {}, method: {} 失败，未能获取到锁", className, methodName);
           return null;
           }
        } catch (Throwable e) {
           e.printStackTrace();
        }
        return obj;
     }
     ```

5. 其他功能
   
   * 客户端连接数限制
   * 总连接数限制
   * 请求总并发限制
   * client请求并发限制（**进行中**）
     
     ```java
     @Test
     public void getSlidingWindowLimiter() {
        String key = "testSlidingWindow";
        SlidingWindowLimiterConfig config = new SlidingWindowLimiterConfig(
           key,
           2,
           10,
           redissonService.getRLock(key),
           redisService
        );
        SlidingWindowLimiter limiter = windowFactory.getSlidingWindowLimiter(config);
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at first time."); // passed
        try {
           System.out.println("After sleep 500 millis--------");
           Thread.sleep(500);
        } catch (Exception e) {
           e.printStackTrace();
        }
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at second time."); // passed
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at third time."); // failed
     }
     ```
