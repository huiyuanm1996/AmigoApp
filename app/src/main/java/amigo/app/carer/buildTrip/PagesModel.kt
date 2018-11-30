package amigo.app

enum class PagesModel(val order: Int, val pageID: Int, val title: String) {
    LOCATION(0, R.layout.location_layout, "Location"),
    DESTINATION(1, R.layout.destination_layout, "Destination"),
    TRANSPORT(2, R.layout.transport_layout, "Transport");

    companion object {
        private val map = PagesModel.values().associateBy(PagesModel::order)
        fun fromInt(type: Int) = map[type] ?: LOCATION
    }

}
