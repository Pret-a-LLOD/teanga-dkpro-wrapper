Teanga DKPro Wrapper
====================

**Work in progress**

How to use 
----------

Build the dockers

    mvn exec:java

Start the individual components (e.g., OpenNLP)

    cd dockers/dkpro-core-opennlp-asl
    mvn install jetty:run

Segment some text

    curl -v -X POST -H "Content-Type: application/json" --data \
    '{"documentText":"this is a test","language":"en"}' \
    http://localhost:8080/OpenNlpSegmenter

POS tag some text

    curl -v -X POST -H "Content-Type: application/json" --data \
    '{"documentText":"this is a test","language":"en","token":[{"begin":0,"end":4},{"begin":5,"end":7},{"begin":8,"end":9},{"begin":10,"end":14}],"sentence":[{"begin":0,"end":14}]}' \
    http://localhost:8080/OpenNlpPosTagger

API description are generated at `dockers/${jar-name}/openapi.yaml`
