# dsty/jenkins:latest

This image serves as the base image for the rest of the containers used by the Jenkins Standard Lib. This image is avaliable publicly from [dsty/jenkins:latest](https://hub.docker.com/repository/docker/dsty/jenkins).

## Building locally

From the root of the repo run:
```sh
docker build -t dsty/jenkins:latest -f docker/prod/Dockerfile .
```

## Using
To run this container:
```sh
docker run -d --rm -p 3000:80 dsty/jenkins:latest
```

You can then see the jenkins at [127.0.0.1:3000](http://127.0.0.1:3000). The password will be in the container logs.
