# Gatling-suorituskykytestaus #

Asenna Gatling 2.0 http://gatling.io/ esim. hakemistoon gatling repon sisällä (hakemisto on git ignoressa)

Muuta conf-hakemistossa gatling.conf osoittamaan perftest hakemistoon:

```
    directory {
      data = ../../perftest/user-files/data                    # Folder where user's data (e.g. files used by Feeders) is located
      requestBodies = ./../perftest/user-files/request-bodies # Folder where request bodies are located
      simulations = ./../perftest/user-files/simulations      # Folder where the bundle's simulations are located
      #reportsOnly = ""                          # If set, name of report folder to look for in order to generate its report
      #binaries = ""                             # If set, name of the folder where compiles classes are located
      #results = results                         # Name of the folder where all reports folder are located
    }
```

## Suorituskykytestian ajaminen

`bin/gatling.sh`

## Suorituskykytestien nauhoittaminen

`bin/recorder.sh`

Aseta selaimesta proxy osoittamaan localhost porttiin 8000