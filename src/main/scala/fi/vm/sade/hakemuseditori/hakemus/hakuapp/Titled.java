package fi.vm.sade.hakemuseditori.hakemus.hakuapp;


public abstract class Titled extends Element {

    private static final long serialVersionUID = 761433927081818640L;

    private I18nText i18nText;
    private I18nText excelColumnLabel;
    private I18nText verboseHelp;
    private I18nText placeholder;

    public Titled(final String id, final I18nText i18nText) {
        super(id);
        this.i18nText = i18nText;
    }

    public I18nText getI18nText() {
        return i18nText;
    }

    public I18nText getVerboseHelp() {
        return verboseHelp;
    }

    public void setVerboseHelp(final I18nText verboseHelp) {
        this.verboseHelp = verboseHelp;
    }

    public I18nText getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(I18nText placeholder) {
        this.placeholder = placeholder;
    }

    public I18nText getExcelColumnLabel() {
        return excelColumnLabel != null
            ? excelColumnLabel
            : i18nText;
    }

    public void setExcelColumnLabel(I18nText excelColumnLabel) {
        this.excelColumnLabel = excelColumnLabel;
    }
}

