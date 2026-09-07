package org.example.workload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            DefaultDbRefResolver defaultDbRefResolver,
            MongoMappingContext context,
            MongoCustomConversions conversions) {
        MappingMongoConverter mappingConverter = new MappingMongoConverter(defaultDbRefResolver, context);
        mappingConverter.setCustomConversions(conversions);
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return mappingConverter;
    }

    @Bean
    public DefaultDbRefResolver defaultDbRefResolver(MongoDatabaseFactory factory){
        return new DefaultDbRefResolver(factory);
    }
}
