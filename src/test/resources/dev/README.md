
Deploy process
```bash
  curl http://localhost:8080/v1/dev/resource/deployments -F resources=@100-start-end.bpmn
```

Create process instance
```bash
    curl -X POST http://localhost:8080/v1/dev/processInstances -H "Content-Type: application/json" -d '{"processId":"StartEndEventId", "processVersion":"1.2.3", "variables": {"a": 1}}'
```

```bash
    curl http://localhost:8080/v1/dev/processInstances/019AF51C-32F2-74E7-BB5D-2A0E6FD851F8 -H "Content-Type: application/json"
```

