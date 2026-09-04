# Deployment configuration

Everything specific to one deployment — which SIP domains get NAT/STUN handling
and how server addresses are aliased — lives outside the tracked source so the
repository can be published.

## Setting it up

```sh
cp deployment.properties.example deployment.properties
```

Then edit `deployment.properties` and fill in your own values. The file is
git-ignored and read by `app/build.gradle.kts` at configure time.

## Building without it

Every setting is optional. With no `deployment.properties` the app builds and
runs normally (Gradle just logs that the file is absent): no NAT policy is
applied, no domain aliasing happens, and users register against whichever SIP
server they type into the Register screen.

## Available settings

| Key | Purpose |
| --- | --- |
| `webb.domainAliases` | Comma-separated `address=canonicalDomain` pairs. |
| `webb.natStunPolicies` | Comma-separated `domain=stunServer` pairs; listed domains get ICE + STUN. |

The values reach the app as `BuildConfig` fields and are parsed at runtime by
`app/src/main/java/ca/webb/mobile/companionapp/voip/android/data/config/DeploymentConfig.kt`.

## Signing material

Keystores, certificates and private keys are git-ignored by extension
(`*.jks`, `*.keystore`, `*.der`, `*.p12`, `*.pem`, …) and must be distributed
out of band — never committed.
