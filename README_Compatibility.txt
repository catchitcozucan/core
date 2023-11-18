
Some notes on compatibility:

This library has now, apart from the JDK on which is was originally written
which was Oracle's last official JDK 8, been tested using the following JDKs
from OpenJDK (https://jdk.java.net):

openjdk version "1.8.0_41"
OpenJDK Runtime Environment (build 1.8.0_41-b04)
OpenJDK Client VM (build 25.40-b25, mixed mode)

openjdk version "9"
OpenJDK Runtime Environment (build 9+181)
OpenJDK 64-Bit Server VM (build 9+181, mixed mode)

openjdk version "10" 2018-03-20
OpenJDK Runtime Environment 18.3 (build 10+44)
OpenJDK 64-Bit Server VM 18.3 (build 10+44, mixed mode)

openjdk version "11" 2018-09-25
OpenJDK Runtime Environment 18.9 (build 11+28)
OpenJDK 64-Bit Server VM 18.9 (build 11+28, mixed mode)

openjdk version "12" 2019-03-19
OpenJDK Runtime Environment (build 12+32)
OpenJDK 64-Bit Server VM (build 12+32, mixed mode, sharing)

openjdk version "13" 2019-09-17
OpenJDK Runtime Environment (build 13+33)
OpenJDK 64-Bit Server VM (build 13+33, mixed mode, sharing)

openjdk version "14" 2020-03-17
OpenJDK Runtime Environment (build 14+36-1461)
OpenJDK 64-Bit Server VM (build 14+36-1461, mixed mode, sharing)

openjdk version "15" 2020-09-15
OpenJDK Runtime Environment (build 15+36-1562)
OpenJDK 64-Bit Server VM (build 15+36-1562, mixed mode, sharing)

As there is presently, though I may work on this, no reliable
ProGuard maven plugin supporting JDKs as from JDK9 and onwards
I have, for the time being, left the ProGuard processing out of
the current build. Hopefully this will be solved in future
releases.

Ola Aronsson
nollettnoll AB
2021-01-28

--

Currently 2023-11-14 I'm running

openjdk version "20.0.1" 2023-04-18
OpenJDK Runtime Environment (build 20.0.1+9-29)
OpenJDK 64-Bit Server VM (build 20.0.1+9-29, mixed mode, sharing)

which works perfectly fine.
