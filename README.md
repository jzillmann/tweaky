# Tweaky

Pieces of a traditional master-node cluster setup with modern technology.

## Build

- `./gradlew generateProto` - Generate protbuf sources
- `./gradlew cleanEclipse eclipse` - Create Eclipse files


## TODO

- √GRPC sample
- √Logging
- √NodeRegistrationValidator
- √Junit5 ExternalResource
- √Implement Node registration
- √Dagger example
- NodeAcceptor
- Dagger for nodes
- Push node-name to node

## Thoughts

- Example projects:
   - Distributed Map
   - Task Queue
- Node Role Support
   - Conductor assigns role(s) to node on registration
   - Node starts services based on roles
- Services
    - Monitoring
    - Monitoring-UI
- Utils
    - CallTracing (from client, to conductor including all node calls. Use metadata. Check https://github.com/grpc-ecosystem/grpc-opentracing)
    - Profiling (custom for apps. just have generic proto files available. Nodes can track their individual work steps)