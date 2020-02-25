# cloud-config-cli-web


At the moment, spring-cloud-config-client artifact only can be implemented in spring-boot projects, so most of us aren't
in the role to migrate the weblogic, websphere, etc enviroments of our clients to spring-boot, the artifac cloud-config-cli-web 
allows us to implement spring-cloud-config-client in our web projects.


1.- Add cloud-config-cli-client

```
<project>        
    ...
        
        <properties>
                
                ...
                
                <!-- cloud-conf-cli-web version -->
                <cloud-conf-cli-web.version>1.0.0</cloud-conf-cli-web.version>
                
        </properties>

        <dependencies>

                ...
                
                <!-- cloud-conf-cli-web -->
                <!-- https://github.com/Ivos-Labs/spring-cloud-conf-cli-web-->
                <dependency>
                        <groupId>com.ivoslabs</groupId>
                        <artifactId>cloud-config-cli-web</artifactId>
                        <version>${cloud-conf-cli-web.version}</version>
                </dependency>
				
                ...
                
        <dependencies>
    ...
</project>
```

1.- Implementing CloudPropertyPlaceholderConfigurer in spring

``` 
 <bean id="propertyConfigurer" class="com.ivoslabs.spring.cloud.config.cl.web.CloudPropertyPlaceholderConfigurer">
	<property name="environment" ref="environment" />
	<property name="locations">
		<list>
			<value>classpath:project.properties</value>
		</list>
	</property>
	
</bean>
```

2.- Add the properties required to connect to a spring-cloud-config-server service  
 
```
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.name=spring_cloud_conf_web_example
spring.cloud.config.profile=dev
spring.cloud.config.username=example-usr
spring.cloud.config.password=example-pwd
```




4.- endpoint: [ip]:[puerto]/[contexto]/actuator/refresh


