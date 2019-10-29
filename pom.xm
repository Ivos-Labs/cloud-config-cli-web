<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
 
  <groupId>com.ivoslabs</groupId>
	<artifactId>cloud-config-cli-web</artifactId>
	<version>1.0.0</version>
 	<packaging>jar</packaging>

	<name>cloud-config-cli-web</name>
	<description>Artefact to read remote properties</description>
	<url>https://github.com/Ivos-Labs/cloud-config-cli-web</url>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
			<distribution>repo</distribution>
		</license>
	</licenses>

	<properties>

		<!-- Generic properties -->
		<maven.compiler.source>1.8</maven.compiler.source>
		<maven.compiler.target>1.8</maven.compiler.target>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

	</properties>

	<dependencies>

		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-client</artifactId>
			<version>1.2.0.RELEASE</version>
		</dependency>

		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-web</artifactId>
			<version>${spring-framework.version}</version>
		</dependency>

		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
			<version>${spring-framework.version}</version>
		</dependency>

		<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>javax.servlet-api</artifactId>
			<version>3.1.0</version>
			<scope>provided</scope>
		</dependency>

	</dependencies>
  
  <organization>
		<name>Ivos Solutions Labs</name>
		<url>https://www.ivoslabs.com</url>
	</organization>

	<developers>
		<developer>
			<id>imperezivan</id>
			<name>Ivan Perez</name>
			<email>iperez@ivoslabs.com</email>
			<organization>Ivos Solutions Labs</organization>
			<organizationUrl>https://www.ivoslabs.com</organizationUrl>
			<url>https://github.com/imperezivan</url>
			<timezone>-5</timezone>
		</developer>
	</developers>

	<scm>
		<connection>scm:git:git://github.com/Ivos-Labs/cloud-config-cli-web.git</connection>
		<developerConnection>scm:git:ssh://github.com:Ivos-Labs/v.git</developerConnection>
		<url>http://github.com/Ivos-Labs/cloud-config-cli-web/tree/master</url>
	</scm>

	<issueManagement>
		<system>GitHub</system>
		<url>https://github.com/Ivos-Labs/cloud-config-cli-web/issues</url>
	</issueManagement>


	<profiles>
		<profile>
			<id>javadoc</id>
			<build>
				<plugins>
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-javadoc-plugin</artifactId>
						<version>3.1.1</version>
						<executions>
							<execution>
								<id>attach-javadocs</id>
								<goals>
									<goal>jar</goal>
								</goals>
								<configuration>
									<tags>
										<tag>
											<name>author</name>
											<placement>a</placement>
											<head>Author</head>
										</tag>
									</tags>
									<show>private</show>
									<doclint>missing</doclint>
 								</configuration>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>
		</profile>

		<profile>
			<id>deploy</id>
			<build>
				<plugins>
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-source-plugin</artifactId>
						<version>3.1.0</version>
						<executions>
							<execution>
								<id>attach-sources</id>
								<goals>
									<goal>jar</goal>
								</goals>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>
		</profile>
	</profiles>

</project>
