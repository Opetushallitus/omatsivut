import moment from 'moment';
import 'moment/locale/fi';
import 'moment/locale/sv';
import 'moment/locale/en-gb';
import { getLanguage } from './staticResources';

const language = getLanguage();
if (language === "en")
  moment.locale("en-gb");
else
  moment.locale(language);

export default moment;
