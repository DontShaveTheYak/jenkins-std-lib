# DSTY demo

This image was created to allow people to easily run the Jenkinst Standard Library locally.

It contains the example jobs and the recommended plugins from the main [README.md](../../README.md).

This image is avaliable publicly from [dsty/jenkins:demo](https://hub.docker.com/repository/docker/dsty/jenkins).

## Building locally

From the root of the repo run:
```sh
docker build -t dsty/jenkins:demo -f docker/demo/Dockerfile .
```

## Using

To run this container:
```sh
docker run -d --rm -p 4000:80 dsty/jenkins:demo
```

You can then see the jenkins at [127.0.0.1:4000](http://127.0.0.1:4000). If the seed job hasn't run, you can run it manually.
