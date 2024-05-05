
release:
	@mvn clean package
	mkdir -p build/jredis/bin
	cp -r clients/py build/jredis/
	@python setup/build.py server > build/jredis/bin/jredis-server
	chmod +x build/jredis/bin/jredis-server
	@python setup/build.py client > build/jredis/bin/jredis-client
	chmod +x build/jredis/bin/jredis-client
	cp target/JRedis-1.0-SNAPSHOT-jar-with-dependencies.jar build/jredis/
	tar -zcvf build/jredis.tar.gz -C build jredis

clean:
	rm -rf build/

.PHONY: release clean
