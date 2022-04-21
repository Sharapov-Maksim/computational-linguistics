## Практическое задание №3 - Построение n-грамм

Входные данные записываются в `task3/src/main/resources/input.txt` в формате:<br>
<значение порога для фильтра>

Код, относящийся к построению n-грамм лежит в пакете `task3/src/main/java/ngramms/`

Пример выходных данных лежит в `task3/src/main/resources/?-gramms_filtered.txt` в случае фильтрованных по устойчивости ?-грамм. А в `task3/src/main/resources/?-gramms.txt` лежат все найденные в корпусе ?-граммы.

Для того, чтобы воспроизвести работу программы, нужно дополнительно положить файл dict.opcorpora.xml в директорию `dictionary`. XML-файл с словарём open corpora можно скачать сдесь: http://opencorpora.org/dict.php.