package com.ylpu.kepler.scheduler.executor.listener;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.ylpu.kepler.scheduler.core.utils.JsonUtils;

public class KafkaMetricProducer implements IMetricProducer{
    
    private Properties props;
        
    private KafkaProducer<String, String> producer;
    
    private static Log LOG = LogFactory.getLog(KafkaMetricProducer.class);
    
    public KafkaMetricProducer(Properties props) {
        this.props = props;
    }
    
    private Properties initProducer(Properties props) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", props.getProperty("kafka.bootstrap.servers"));
        kafkaProperties.put("acks", "all");
        kafkaProperties.put("retries", 0);
        kafkaProperties.put("batch.size", 16384);
        kafkaProperties.put("linger.ms", 1);
        kafkaProperties.put("buffer.memory", 33554432);
        kafkaProperties.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProperties.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        return kafkaProperties;
    }
    
    public void send(Event event) {
        try {
            Properties kafkaProperties = initProducer(props);
            producer = new KafkaProducer<String, String>(kafkaProperties);
            String data = JsonUtils.objToJson(event);
            String topic = props.getProperty("kafka.topic");
            producer.send(new ProducerRecord<String, String>(topic, data));
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if(producer != null) {
                producer.close();
            }
        }
    }
}
