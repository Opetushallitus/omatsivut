package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.elements;

public class Notification extends Titled {

    private static final long serialVersionUID = -235576061716035350L;

    public enum NotificationType {
        WARNING("warning"), INFO("info");

        private final String type;

        NotificationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return type;
        }

    }

    private Notification.NotificationType notificationType;

    public Notification(final String id, final I18nText i18nText, final Notification.NotificationType notificationType) {
        super(id, i18nText);
        this.notificationType = notificationType;
    }

    public Notification.NotificationType getNotificationType() {
        return notificationType;
    }
}
