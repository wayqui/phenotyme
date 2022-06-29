# CoreNLP API

This is a microservice that performs NER using CoreNLP and SUTime in spanish. ITs aim is to identify and tag temporal expressions.

# Getting Started

Since this Spring Boot service is containerized you should execute the following commands to run it

```
docker build -t core-nlp-api .
```

To run the image just execute

```
docker run -d -p 8080:8080 core-nlp-api
```

# Licence

GNU General Public License v3.0