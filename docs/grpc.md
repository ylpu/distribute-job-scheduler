//根据proto生成java,pb目前用3.2版本
Protoc --proto_path=C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\proto --java_out=C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\java\ C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\proto\job.proto

//使用protoc-gen-grpc-java插件根据proto生成service,pb目前用3.2版本
protoc --plugin=protoc-gen-grpc-java=C:\Users\yupu.CORP\Downloads\protoc-gen-grpc-java-1.9.0-windows-x86_64.exe --grpc-java_out=C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\java\com\ylpu\thales\scheduler\core\rpc\service --proto_path=C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\proto\ C:\distribute-job-scheduler\thales-scheduler\scheduler-core\src\main\proto\job.proto