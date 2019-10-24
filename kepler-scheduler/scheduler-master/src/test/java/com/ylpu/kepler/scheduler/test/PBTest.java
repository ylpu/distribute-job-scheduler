package com.ylpu.kepler.scheduler.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.google.protobuf.ByteString;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;

public class PBTest {
	
	public static byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                }
            }

        }
        return bytes;
    }

    /**
     * Byte数组转对象
     * @param bytes
     * @return
     */
    public static Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return obj;
    }

	public static void main(String[] args) {
		JobInstanceRequest request = new JobInstanceRequest();
		request.setLogPath("/var/log/app.log");
		JobInstanceResponseRpc response = JobInstanceResponseRpc.newBuilder()
        .setData(ByteString.copyFrom(objectToByteArray(request)))
        .build();
		JobInstanceRequest request1 = (JobInstanceRequest) byteArrayToObject(response.getData().toByteArray());
		System.out.println(request1.getLogPath());
	}
}
