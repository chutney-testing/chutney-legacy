### build docker images

from project root folder, run:
**server**

```shell
docker build --tag ghcr.io/chutney-testing/chutney/server:latest . -f ./.docker/server/Dockerfile
```

**ui**

```shell
docker build --tag ghcr.io/chutney-testing/chutney/ui:latest . -f ./.docker/ui/Dockerfile
```

### push docker image to github registry

-   push will be done by github actions during release workflow
-   it's possible to push manually :

```shell
//login
docker login ghcr.io -u ${your_username} --password ${your_personal_github_token}
// push
docker push ghcr.io/chutney-testing/chutney/server:latest
docker push ghcr.io/chutney-testing/chutney/ui:latest
```

### run ui and server containers using docker compose

```shell
docker-compose -f ./.docker/docker-compose-local-dev.yml up -d
```

**notes:**

-   by default server container will run with local-dev configuration(see packaging/local-dev module)
-   it's possible to override default configuration by passing configuration folder as volume when running server container (see docker-compose-custom-config.yml file for more details)

### enjoy app

visit https://localhost

### Stop & remove docker compose services

**stop**

```shell
docker-compose -f ./.docker/docker-compose-local-dev.yml stop
```

**remove**

```shell
docker-compose -f ./.docker/docker-compose-local-dev.yml rm server ui --force
```
