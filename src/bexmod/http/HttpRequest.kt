package bexmod.http

class HttpRequest(request: String) {
    var method = Method.UNINITIALIZED
        private set
    var version = Version.VNA
        private set
    var resource = Resource("")
        private set
    var headers = mutableMapOf<String, String>()
        private set
    var body = ""
        private set

    init {
        request.lines()
            .forEach {
                if (it.contains("HTTP")) {
                    val (m, v, r) = processRequest(it)
                    method = m
                    version = v
                    resource = r
                } else if (it.contains(":")) {
                    val (key, value) = processHeader(it)
                    headers[key] = value
                } else if (it.isNotBlank()) {
                    body = it
                }
            }
    }

    private fun processRequest(line: String): Triple<Method, Version, Resource> {
        val pcs = line.split(" ")
        if (pcs.size != 3)
            throw Exception("Missing some args")

        return Triple(
            Method.valueOf(pcs[0]),
            Version.valueOf(pcs[1]),
            Resource(pcs[2])
        )
    }

    private fun processHeader(line: String): Pair<String, String> {
        val pcs = line.split(":")
        return if (pcs.size > 1) Pair(pcs[0], pcs[1]) else Pair(pcs[0], "")
    }
}