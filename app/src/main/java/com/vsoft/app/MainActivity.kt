@Composable
fun ChartBar(
    label: String,
    value: Long,
    maximum: Float,
    modifier: Modifier
) {
    val ratio =
        value.toFloat() / maximum

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            NumberFormat
                .getNumberInstance(
                    Locale("fa", "IR")
                )
                .format(value),
            fontSize = 11.sp
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    max(
                        5f,
                        110f * ratio
                    ).dp
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .primary
                )
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Text(
            label,
            fontWeight = FontWeight.Bold
        )
    }
}
