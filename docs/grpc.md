//根据proto生成java,pb目前用3.2版本
Protoc --proto_path=/Users/admin/thales/thales-scheduler/scheduler-core/src/main/proto --java_out=/Users/admin/thales/thales-scheduler/scheduler-core/src/main/java/ /Users/admin/thales/thales-scheduler/scheduler-core/src/main/proto/job.proto

//使用protoc-gen-grpc-java插件根据proto生成service,pb目前用3.2版本
protoc --proto_path=/Users/admin/thales/thales-scheduler/scheduler-core/src/main/proto  --plugin=protoc-gen-grpc-java=//Users/admin/Downloads/protoc-gen-grpc-java --grpc-java_out=./ --proto_path=./ /Users/admin/thales/thales-scheduler/scheduler-core/src/main/proto/job.proto