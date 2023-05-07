setup:
	gradle wrapper --gradle-version 7.5.1

clean:
	./gradlew clean

build:
	./gradlew clean build

run:
	APP_ENV=development ./gradlew run

install:
	./gradlew install

run-dist:
	APP_ENV=production ./build/install/app/bin/app

generate-migrations:
	./gradlew generateMigrations

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

.PHONY: build
