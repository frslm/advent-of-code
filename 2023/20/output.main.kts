import java.io.File

enum class Pulse { Low, High }

abstract class Module(val destinations: List<String>) {
    abstract fun process(pulse: Pulse, source: String): Pulse?

    protected val inputs: MutableSet<String> = mutableSetOf()
    fun connectInputs(inputs: Set<String>) {
        inputs.forEach { this.inputs.add(it) }
    }
}

class Broadcaster(destinations: List<String>) : Module(destinations) {
    override fun process(pulse: Pulse, source: String) = pulse
}

class FlipFlop(destinations: List<String>) : Module(destinations) {
    enum class State { Off, On }
    private var state = State.Off
    override fun process(pulse: Pulse, source: String) =
        when (pulse) {
            Pulse.Low -> when (state) {
                State.Off -> Pulse.High.also { state = State.On }
                State.On -> Pulse.Low.also { state = State.Off }
            }
            Pulse.High -> null
        }
}

class Conjunction(destinations: List<String>) : Module(destinations) {
    private val lastReceivedHighFrom = mutableSetOf<String>()
    override fun process(pulse: Pulse, source: String) =
        when (pulse) {
            Pulse.Low -> lastReceivedHighFrom.remove(source)
            Pulse.High -> lastReceivedHighFrom.add(source)
        }.run {
            if (lastReceivedHighFrom == inputs) Pulse.Low else Pulse.High
        }
}

val modules = File("input.txt").readLines().associate { line ->
    val (module, destinations) = line.split(" -> ").let { it[0] to it[1].split(", ") }
    when (module) {
        "broadcaster" -> module to Broadcaster(destinations)
        else -> module.substring(1) to when (val symbol = module.first()) {
            '%' -> FlipFlop(destinations)
            '&' -> Conjunction(destinations)
            else -> throw Exception("Unknown module type $symbol!")
        }
    }
}.also {
    it.forEach { (name, module) ->
        module.connectInputs(it.filter { other -> name in other.value.destinations }.keys)
    }
}

// Part 1:

// General idea is to use an event queue to ensure the timing of pulses is correct.

data class Propagation(val pulse: Pulse, val source: String, val destination: String)

fun Iterable<Propagation>.propagateThrough(modules: Map<String, Module>, onPropagate: Propagation.() -> Unit) {
    val queue = this.toMutableList()
    while (queue.isNotEmpty()) {
        val (pulse, source, destination) = queue.removeFirst().also { it.onPropagate() }

        modules[destination]?.let { module ->
            module.process(pulse, source)?.let { output ->
                module.destinations.forEach { queue.add(Propagation(output, destination, it)) }
            }
        }
    }
}

var low = 0L
var high = 0L

repeat (1000) {
    listOf(Propagation(Pulse.Low, "", "broadcaster"))
        .propagateThrough(modules) { when (pulse) { Pulse.Low -> low++; Pulse.High -> high++ } }
}

println(low * high)

// Part 2:
println(

)
