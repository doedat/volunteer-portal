package au.org.ala.volunteer

enum TaskFilter {

    showReadyForTranscription("taskFilterType.readyForTranscription"),
    showTranscriptionLocked("taskFilterType.transcriptionLocked"),
    showAll("taskFilterType.showAll")

    String i18nLabel

    TaskFilter(String i18nLabel) {
        this.i18nLabel = i18nLabel
    }

}
