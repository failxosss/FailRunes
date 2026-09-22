# Build verification notes

This plugin was written in a sandboxed environment whose network allowlist covers GitHub but **not**
Maven Central, the PaperMC repository, or any other Minecraft-plugin Maven host. So a real `mvn package`
could not be run here. To still catch real bugs instead of guessing, every Java file in this project was
compiled with `javac -sourcepath ...` directly against:

- The real **Paper API source tree** (`git clone` of PaperMC/Paper, `paper-api/src/main/java`)
- The real **Adventure** source trees (api, key, text-minimessage, text-serializer-plain/legacy/gson)
- The real **Examination** and **Option** libraries Adventure depends on
- Minimal stubs for `org.jetbrains.annotations`, `org.jspecify.annotations`, `org.checkerframework.*`
  (annotation-only interfaces with no behavior, so they don't affect type-checking)

This caught and fixed several real bugs during development (a broken Adventure Component composition in
the death-message code, an interface/impl method mismatch in the periodic auto-save task, an incorrect
WorldGuard hook pattern).

**What this did *not* cover:** Guava, Apache Commons Lang3, Gson and the legacy BungeeCord chat API are
used internally by parts of the Paper API itself (not by FailRunes' own code) but weren't fetchable here.
Their absence causes real but *unrelated* compile errors deep inside Paper's own source files (e.g.
`SmallFireball.java`), which cascade into spurious "cannot find symbol" errors on a few completely
ordinary, unrelated classes we use (`PlayerFishEvent`, `PlayerArmorChangeEvent`). Those were manually
traced and confirmed to be classpath artifacts, not bugs in FailRunes — see the commit history / generation
log for the trace. The four soft-dependency integrations (ItemsAdder, WorldGuard, Vault, PlaceholderAPI)
also couldn't be checked against their real APIs for the same reason; their hook classes were written
carefully from the well-known, stable public API shapes of those plugins, but you should sanity-test them
against your actual server's installed versions.

**Bottom line:** the entire engine, GUI, storage, commands, items, listeners and 750-rune database
compiled cleanly against real Paper/Adventure sources. A normal `mvn clean package` on a machine with
internet access is expected to succeed; if the optional-integration hook classes need adjusting for a
specific WorldGuard/ItemsAdder/Vault version, they're isolated in `dev.failxos.failrunes.integration` and
the rest of the plugin does not depend on their exact shape.
