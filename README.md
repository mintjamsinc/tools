# Tools
MintJams Tools is an MIT-licensed open source project with a set of basic libraries for creating Java applications.

## Example

General-purpose collections

```java
AdaptableMap<String, Object> map = AdaptableMap
    .<String, Object>newBuilder()
    .put("String", "Tools")
    .put("Byte", Byte.MAX_VALUE)
    .put("Short", Short.MAX_VALUE)
    .put("Integer", Integer.MAX_VALUE)
    .put("Long", Long.MAX_VALUE)
    .put("Float", (float) 1.0001)
    .put("Double", (double) 1.000000000002)
    .put("Long", Long.MAX_VALUE)
    .put("BigDecimal", new BigDecimal("1.000000000003"))
    .put("BigInteger", new BigInteger("999999999999"))
    .put("OffsetDateTime", OffsetDateTime.now())
    .put("LocalDateTime", LocalDateTime.now())
    .put("LocalDate", LocalDate.now())
    .put("OffsetTime", OffsetTime.now())
    .put("LocalTime", LocalTime.now())
    .put("Date", new java.util.Date())
    .put("SQLDate", new java.sql.Date(System.currentTimeMillis()))
    .put("SQLTime", new java.sql.Time(System.currentTimeMillis()))
    .put("SQLTimestamp", new java.sql.Timestamp(System.currentTimeMillis()))
    .put("Boolean", Boolean.TRUE)
    .build();

for (String key : map.keySet()) {
  // Gets the value as a String and outputs it.
  System.out.println(key + " (" + map.get(key).getClass().getName() + "): \"" + map.adapt(key, String.class).getValue() + "\"");
}

AdaptableList<Object> list = AdaptableList
    .<Object>newBuilder()
    .addAll(map.values())
    .build();

for (int i = 0; i < list.size(); i++) {
  // Gets the value as a String and outputs it.
  System.out.println("" + i + " (" + list.get(i).getClass().getName() + "): \"" + list.adapt(i, String.class).getValue() + "\"");
}
```

Executing an SQL select statement

```java
Query query = Query
    .newBuilder()
    .setStatement("SELECT * FROM items WHERE price >= {{minPrice}}")
    .setVariable("minPrice", 200)
    .setConnection(connection)
    .build();
try (Result result = query.setOffset(0).setLimit(1000).execute()) {
  for (AdaptableMap<String, Object> e : result) {
    // Gets the value as a String.
    String name = e.getString("name");

    // Gets the value as a long.
    long price = e.getLong("price");

    // Gets the value as a Date.
    java.util.Date releaseDate = e.getDate("release_date");
  }
}
```

Executing an SQL update statement

```java
int updated = Update
    .newBuilder()
    .setStatement("UPDATE items SET price = {{newPrice}} WHERE id = {{id}}")
    .setVariable("id", "101")
    .setVariable("newPrice", 100)
    .setConnection(connection)
    .build()
    .execute();
```

Using schema auto-detection

```java
Entity entity = Entity
    .newBuilder()
    .setName("items")
    .setConnection(connection)
    .build();

AdaptableMap<String, Object> pk = AdaptableMap
    .<String, Object>newBuilder()
    .put("id", "101")
    .build();

try (Result result = entity.findByPrimaryKey(pk).execute()) {
  AdaptableMap<String, Object> row = result.iterator().next();

  // The column type is automatically detected and the value is converted appropriately.
  row.put("release_date", "2021-03-23T01:00:00.000Z");

  int updated = entity.update(row, pk).execute();
  connection.commit();
}
```

## License

[MIT](https://opensource.org/licenses/MIT)

Copyright (c) 2021 MintJams Inc.
