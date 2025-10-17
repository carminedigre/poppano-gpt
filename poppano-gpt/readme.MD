Pre-Requisites: at least 16GB of RAM + graalVM (or OpenJDK) + docker + ollama (models llama3:8b & mxbai-embed-large:335m)


1 - Build: "mvnw clean install" or "mvn -Pnative native:compile"
2 - docker compose up  
3 - in the target folder: "java -jar poppano-gpt.jar" or "./poppano-gpt"
4 - http://localhost:8080 (index web page to chat)


add file in the rag: 

curl -F "file=@/your/path/doc.pdf" http://localhost:8080/api/ingest

query:

curl -G "http://localhost:8080/api/ask" \
  --data-urlencode "q=your question" \
  --data-urlencode "k=8"