
[![Build Status](https://travis-ci.com/felipegutierrez/bidding-system.svg?branch=main)](https://travis-ci.com/felipegutierrez/bidding-system)
[![Coverage Status](https://coveralls.io/repos/github/felipegutierrez/bidding-system/badge.svg)](https://coveralls.io/github/felipegutierrez/bidding-system)
[![CodeFactor](https://www.codefactor.io/repository/github/felipegutierrez/bidding-system/badge)](https://www.codefactor.io/repository/github/felipegutierrez/bidding-system)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df38ef7ca2764f78b5ab73e1c16c9024)](https://app.codacy.com/gh/felipegutierrez/bidding-system?utm_source=github.com&utm_medium=referral&utm_content=felipegutierrez/bidding-system&utm_campaign=Badge_Grade)
![Lines of code](https://img.shields.io/tokei/lines/github/felipegutierrez/bidding-system)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/felipeogutierrez/bidding-system)

Bidding system
==============

Yieldlab is a technology service provider, connecting suppliers (those who
have space to show ads, e.g. on their websites) to bidders (those who actually
want to show ads). The core process is to listen for requests, gather metadata
and bids, and afterwards to determine who is winning. This challenge is about
setting up a simplified version of this core process as its own application.

Contents of this document:

- [1 The task](#1-the-task)
  - [1.1 Incoming Requests](#11-incoming-requests)
  - [1.2 Bid Requests](#12-bid-requests)
  - [1.3 Bid Response](#13-bid-response)
  - [1.4 Auction Response](#14-auction-response)
  - [1.5 Configuration](#15-configuration)
- [2 How to test your application](#2-how-to-test-your-application)
  - [2.1 Prerequisites](#21-prerequisites)
  - [2.2 Start the Docker containers](#22-start-the-docker-containers)
  - [2.3 Start the application](#23-start-the-application)
  - [2.4 Run the test](#24-run-the-test)
- [3 Bidding system in action](#3-in-action)
  - [3.1 Requirements](#31-requirements)
  - [3.2 Quick test](#32-quick-test)
  - [3.3 Fault tolerant system](#33-fault-tolerant)


## 1 The task

Build a bidding system behaving as following:

For every incoming request as described in [1], send out bid requests as
described in [2] to a configurable number of bidders [5]. Responses from these
bidders as described in [3] must be processed. The highest bidder wins, and
payload is sent out as described in [4].

Incoming and outgoing communication is to be done over HTTP. Message formats
are described below.

Please write code that you would want to maintain in production as well, or
document all exceptions to this rule and give reasons as to why you made those
exceptions.

Please stay with commonly known frameworks for easier reviewing and explaining
afterwards.

[1]: #1-incoming-requests
[2]: #2-bid-requests
[3]: #3-bid-response
[4]: #4-auction-response

### 1.1 Incoming Requests

The application must listen to incoming HTTP requests on port 8080.

An incoming request is of the following format:

    http://localhost:8080/[id]?[key=value,...]

The URL will contain an ID to identify the ad for the auction, and a number of
query-parameters.

### 1.2 Bid Requests

The application must forward incoming bid requests by sending a corresponding
HTTP POST request to each of the configured bidders with the body in the
following JSON format:

```json
{
	“id”: $id,
	“attributes” : {
		“$key”: “$value”,
		…
	}
}
```

The property `attributes` must contain all incoming query-parameters.
Multi-value parameters need not be supported. 
Test is after starting the bidders using the `scripts/test-setup.sh` script, then send some json file:
```
http POST localhost:[8081|8082|8083] < src/main/resources/bidders-request-[1|2|3|4|5|6|7|8|9|10|11].json
```

### 1.3 Bid Response

The bidders' response will contain details of the bid(offered price), with `id` and `bid`
values in a numeric format:

```json
{
	"id" : $id,
	"bid": bid,
	"content": "the string to deliver as a response"
}
```

### 1.4 Auction Response

The response for the auction must be the `content` property of the winning bid,
with some tags that can be mentioned in the content replaced with respective values.

For now, only `$price$` must be supported, denoting the final price of the bid.

Example:


Following bid responses:
```json
{
	"id" : 123,
	"bid": 750,
	"content": "a:$price"
}
```
and

```json
{
	"id" : 123,
	"bid": 500,
	"content": "b:$price"
}
```
will produce auction response as string:
a:750

### 1.5 Configuration

The application should have means to accept accept a number of configuration
parameters. For the scope of this task, only one parameter is to be supported:

| Parameter | Meaning                                                  |
|-----------|----------------------------------------------------------|
| `bidders` | a comma-separated list of URLs denoting bidder endpoints |


## 2 How to test your application

In order to test your application for correctness, a simple test suite is
provided.

### 2.1 Prerequisites

First, a set of bidders is required that will respoond to bidding requests
sent out by your application. For this test suite, we will be using a
pre-built [Docker][what-is-docker] image that will be started several times
with sligthly different configuration values.

Moreover, we provide a shell script that executes the tests and verifies the
test results. That shell script requires the `curl` and `diff` binaries to be
in your `PATH`.

So, here is a list of the requirements:

- Docker ([official installation docs][install-docker])
- A shell (or you'll need to carry out the tests manually)
- `diff` (e.g. from [GNU Diffutils][diffutils] package)
- `curl` ([official download link][curl-dl])

[what-is-docker]: https://www.docker.com/what-docker
[install-docker]: https://docs.docker.com/engine/installation/
[diffutils]: https://www.gnu.org/software/diffutils/
[curl-dl]: https://curl.haxx.se/download.html

### 2.2 Start the Docker containers

To start the test environment, either use the script `test-setup.sh` or run the
following commands one after the other from your shell:

```sh
docker run --rm -e server.port=8081 -e biddingTrigger=a -e initial=150 -p 8081:8081 yieldlab/recruiting-test-bidder &
docker run --rm -e server.port=8082 -e biddingTrigger=b -e initial=250 -p 8082:8082 yieldlab/recruiting-test-bidder &
docker run --rm -e server.port=8083 -e biddingTrigger=c -e initial=500 -p 8083:8083 yieldlab/recruiting-test-bidder &
```

This will set up three bidders on localhost, opening ports 8081, 8082 and 8083.

### 2.3 Start the application

You can use the following configuration parameters to connect to these bidders
from your application:

| Parameter | Value                                                                 |
|-----------|-----------------------------------------------------------------------|
| `bidders` | `http://localhost:8081, http://localhost:8082, http://localhost:8083` |

### 2.4 Run the test

To run the test, execute the shell script `run-test.sh`. The script expects
your application to listen on `localhost:8080`. It will issue a number of bid
requests to your application and verify the responses to these requests. If
your application doesn't respond correctly, it will print out a diff between
the expected and the actual results.

## 3 Bidding system in action

### 3.1 Requirements

 - JDK version 1.8+
 - scala version 2.12
 - sbt version 1.4.7+

### 3.2 Quick test

After start the bidders using the script `test-setup.sh`, start the Bidding system by running the following command. 
Please use `"`, the argument name `--bidders`. and separate the bidders by a single `,` without spaces.

```
sbt "run --bidders http://localhost:8081,http://localhost:8082,http://localhost:8083"
```
Run the generic test using the script `run-test.sh` and make sure that the 3 bidders are running.

### 3.3 Fault tolerant system

The Bidding system accepts bidders using a fault tolerance approach. 
In case that some or all bidders passed as argument to the Bidding System are not available, the Bidden System will compute the highest bid based on the bidders that are responding before 5 seconds. 
If the answer of a bidder last more than 5 seconds it will be considered a null bid and will not be processed. 
Hence, the bid request will never fail, regardless there are bidders available or not. 
Test it by killing some bidder(s) already running (i.e., that were passed in the argument list) and issue single HTTP GET command available at the script `run-test.sh`.
The bid offer may change depending on the bidders that are available.

### 3.4 Docker image

We use the [sbt-native-packager][sbt-native-packager] to generate the Docker image of the [Bidding System][bidding-system-image].
```
sbt docker:stage
sbt docker:publishLocal
docker images
 
REPOSITORY                        TAG                     IMAGE ID       CREATED          SIZE
felipeogutierrez/bidding-system   0.1                     5284993293f2   20 seconds ago   127MB

docker run --rm --add-host host.docker.internal:host-gateway -i -p 8080:8080 felipeogutierrez/bidding-system:0.1
```

[sbt-native-packager]: https://www.scala-sbt.org/sbt-native-packager/
[bidding-system-image]: https://hub.docker.com/repository/docker/felipeogutierrez/bidding-system



