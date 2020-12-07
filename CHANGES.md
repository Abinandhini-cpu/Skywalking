Changes by Version
==================
Release Notes.

8.4.0
------------------
#### Project
* Chore: adapt `create_source_release.sh` to make it runnable on Linux.

#### Java Agent
* Fix jdk-http and okhttp-3.x plugin did not overwrite the old trace header.

#### OAP-Backend
* Make meter receiver support MAL.
* Support Kafka MirrorMaker 2.0 to replicate topics between Kafka clusters.

#### UI
* Fix un-removed tags in trace query.
* Fix unexpected metrics name on single value component.
* Don't allow negative value as the refresh period.
* Fix style issue in trace table view.
* Separation Log and Dashboard selector data to avoid conflicts.
* Fix trace instance selector bug.

#### Documentation


All issues and pull requests are [here](https://github.com/apache/skywalking/milestone/68?closed=1)

------------------
Find change logs of all versions [here](changes).
