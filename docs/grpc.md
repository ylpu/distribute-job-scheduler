//安装protoc
PROTOC_ZIP=protoc-3.2.0-osx-x86_64.zip
curl -OL https://github.com/google/protobuf/releases/download/v3.2.0/$PROTOC_ZIP
sudo unzip -o $PROTOC_ZIP -d /usr/local bin/protoc
sudo unzip -o $PROTOC_ZIP -d /usr/local include/*
rm -f $PROTOC_ZIP

//根据proto生成java,pb目前用3.2版本
protoc --proto_path=/Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/proto --java_out=/Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/java/ /Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/proto/job.proto

//使用protoc-gen-grpc-java插件根据proto生成service,pb目前用3.2版本
protoc --plugin=protoc-gen-grpc-java=/Users/yupu/Downloads/protoc-gen-grpc-java --grpc-java_out=/Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/java/com/ylpu/thales/scheduler/core/rpc/service --proto_path=/Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/proto/ /Users/yupu/Downloads/distribute-job-scheduler/thales-scheduler/scheduler-core/src/main/proto/worker.proto