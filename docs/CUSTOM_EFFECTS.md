# Extending the effect and condition engine

Both registries are static and can be used by any plugin loaded after FailRunes (add FailRunes to your
`depend:`/`softdepend:` in `plugin.yml`).

## New effect type

```java
EffectRegistry.register("MY_EFFECT", (ctx, spec) -> {
    ctx.player.sendMessage("Hi from a custom effect! level=" + ctx.level);
});
```

Use it from any rune's YAML with `type: MY_EFFECT`.

## New condition key

```java
ConditionParser.register("my-condition", (arg, unused) -> ctx -> ctx.player.getWorld().getName().equals(arg));
```

Use it in any rune's `conditions:` list as `my-condition:some-world`.
