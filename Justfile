checkstyle:
    ./gradlew checkstyleMain

test:
    ./gradlew test

check:
    just checkstyle
    just test