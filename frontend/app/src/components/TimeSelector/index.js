import _ from 'lodash';
import moment from 'moment';
import $ from 'npm-zepto';
import React, { PropTypes } from 'react';
import StaffjoyButton from 'components/StaffjoyButton';
import StaffjoyTextField from 'components/StaffjoyTextField';
import {
  MOMENT_SHIFT_CARD_TIMES,
  API_TIME_FORMAT,
} from 'constants/config';
import { getHoursFromMeridiem } from 'utility';
import TimeSelectorNumberButton from './NumberButton';

require('./time-selector.scss');

class TimeSelector extends React.Component {

  constructor(props) {
    const startMoment = moment.utc(props.start, API_TIME_FORMAT)
                        .tz(props.timezone);
    const start = (startMoment.isValid()) ?
      startMoment.format(MOMENT_SHIFT_CARD_TIMES) : '';
    const stopMoment = moment.utc(props.stop, API_TIME_FORMAT)
                       .tz(props.timezone);
    const stop = (stopMoment.isValid()) ?
      stopMoment.format(MOMENT_SHIFT_CARD_TIMES) : '';

    super(props);
    this.state = {
      activeField: 'start',
      startFieldText: start,
      stopFieldText: stop,
      startHour: '',
      startMinute: '',
      startMeridiem: '',
      stopHour: '',
      stopMinute: '',
      stopMeridiem: '',
    };
    this.textFieldOnFocus = this.textFieldOnFocus.bind(this);
    this.textFieldOnChange = this.textFieldOnChange.bind(this);
    this.textFieldOnBlur = this.textFieldOnBlur.bind(this);
    this.timeButtonOnClick = this.timeButtonOnClick.bind(this);
    this.attemptToUpdateTime = this.attemptToUpdateTime.bind(this);
    this.getMomentState = this.getMomentState.bind(this);
  }

  componentDidMount() {
    const { startFieldText, stopFieldText } = this.state;
    this.attemptToUpdateTime('start', startFieldText);
    this.attemptToUpdateTime('stop', stopFieldText);
  }

  getMomentState(field) {
    const stateHour = this.state[`${field}Hour`];
    const stateMinute = this.state[`${field}Minute`];
    const stateMeridiem = this.state[`${field}Meridiem`] || 'am';
    const momentState = moment(this.props.date || '2016-02-01');

    if (stateHour !== '') {
      momentState.hour(
        parseInt(stateHour, 10) +
        getHoursFromMeridiem(stateMeridiem)
      );
    }

    if (stateMinute !== '') {
      momentState.minute(parseInt(stateMinute, 10));
    }

    return momentState;
  }

  timeButtonOnClick(event) {
    const { formCallback } = this.props;
    const { activeField } = this.state;
    const $target = $(event.target);
    const timeSpec = $target.data('time-spec');
    const value = $target.data('time-value');
    const momentState = this.getMomentState(activeField);
    const stateMeridiem = this.state[`${activeField}Meridiem`];

    if (timeSpec === 'Meridiem') {
      let adjustment = 0;
      if (stateMeridiem === 'am' && value === 'pm') {
        adjustment = 12;
      } else if (stateMeridiem === 'pm' && value === 'am') {
        adjustment = -12;
      }
      momentState.add(adjustment, 'hours');
    } else if (timeSpec === 'Hour') {
      momentState.hour(
        parseInt(value, 10) + getHoursFromMeridiem(stateMeridiem)
      );
    } else if (timeSpec === 'Minute') {
      momentState.minute(value);
    }

    const hourValue = (momentState.format('h') === '12') ?
      '0' : momentState.format('h');

    const updatedState = {
      [`${activeField}Hour`]: hourValue,
      [`${activeField}Minute`]: momentState.format('mm'),
      [`${activeField}Meridiem`]: momentState.format('a'),
      [`${activeField}FieldText`]: momentState.format(MOMENT_SHIFT_CARD_TIMES),
    };

    this.setState(updatedState);
    formCallback(updatedState);
  }

  textFieldOnFocus(event) {
    const field = $(event.target).data('field-name');
    const { activeField } = this.state;

    if (field !== activeField) {
      this.setState({ activeField: field });
    }
  }

  textFieldOnChange(event) {
    const field = $(event.target).data('field-name');
    const value = event.target.value;
    this.setState({ [`${field}FieldText`]: value });
  }

  textFieldOnBlur(event) {
    const $target = $(event.target);
    const field = $target.data('field-name');
    const text = $target.val();
    this.attemptToUpdateTime(field, text);
  }

  attemptToUpdateTime(field, text) {
    const { formCallback } = this.props;
    const newState = {};
    const textMoment = moment(text, MOMENT_SHIFT_CARD_TIMES);
    const momentState = this.getMomentState(field);
    const stateHour = this.state[`${field}Hour`];
    const stateMinute = this.state[`${field}Minute`];
    let fieldText = '';

    // update state if valid
    if (textMoment.isValid()) {
      const hourValue = (textMoment.format('h') === '12') ?
        '0' : textMoment.format('h');

      newState[`${field}Hour`] = hourValue;
      newState[`${field}Minute`] = textMoment.format('mm');
      newState[`${field}Meridiem`] = textMoment.format('a');
      fieldText = textMoment.format(MOMENT_SHIFT_CARD_TIMES);

    // attempt to display existing state
    } else if (stateHour !== '' && stateMinute !== '') {
      fieldText = momentState.format(MOMENT_SHIFT_CARD_TIMES);
    }

    newState[`${field}FieldText`] = fieldText;

    this.setState(newState);
    formCallback(newState);
  }

  render() {
    const { activeField, startFieldText, stopFieldText, startMeridiem,
      stopMeridiem, startHour, stopHour, startMinute,
      stopMinute } = this.state;
    const startFocused = activeField === 'start';
    const stopFocused = !startFocused;
    const meridiem = (startFocused) ? startMeridiem : stopMeridiem;
    const hour = (startFocused) ? startHour : stopHour;
    const minute = (startFocused) ? startMinute : stopMinute;

    return (
      <div className="time-selector">
        <div className="input-fields">
          <StaffjoyTextField
            label="Shift Start"
            data-field-name="start"
            onChange={this.textFieldOnChange}
            onFocus={this.textFieldOnFocus}
            onBlur={this.textFieldOnBlur}
            width={160}
            value={startFieldText}
            isFocused={startFocused}
          />
          <StaffjoyTextField
            label="Shift End"
            data-field-name="stop"
            onChange={this.textFieldOnChange}
            onFocus={this.textFieldOnFocus}
            onBlur={this.textFieldOnBlur}
            value={stopFieldText}
            width={160}
            isFocused={stopFocused}
          />
        </div>
        <div className="selector-buttons">
          <div className="meridiem-buttons">
            <StaffjoyButton
              size="tiny"
              buttonType="outline"
              active={meridiem === 'am'}
              onClick={this.timeButtonOnClick}
              data-time-spec="Meridiem"
              data-time-value="am"
            >
              AM
            </StaffjoyButton>
            <StaffjoyButton
              size="tiny"
              buttonType="outline"
              active={meridiem === 'pm'}
              onClick={this.timeButtonOnClick}
              data-time-spec="Meridiem"
              data-time-value="pm"
            >
              PM
            </StaffjoyButton>
          </div>
          <ul className="hour-buttons wrap-list">
            {
              _.map(_.range(1, 13), (value) => {
                const inputValue = (value === 12) ? '0' : String(value);
                const liKey = `time-button-li-${inputValue}`;
                const buttonKey = `time-button-${inputValue}`;
                return (
                  <li key={liKey}>
                    <TimeSelectorNumberButton
                      key={buttonKey}
                      display={value}
                      dataValue={inputValue}
                      currentValue={hour}
                      onClick={this.timeButtonOnClick}
                      data-time-spec="Hour"
                    />
                  </li>
                );
              })
            }
          </ul>
          <ul className="minute-buttons wrap-list">
            <li>
              <TimeSelectorNumberButton
                display=":00"
                dataValue="00"
                currentValue={minute}
                onClick={this.timeButtonOnClick}
                data-time-spec="Minute"
              />
            </li>
            <li>
              <TimeSelectorNumberButton
                display=":15"
                dataValue="15"
                currentValue={minute}
                onClick={this.timeButtonOnClick}
                data-time-spec="Minute"
              />
            </li>
            <li>
              <TimeSelectorNumberButton
                display=":30"
                dataValue="30"
                currentValue={minute}
                onClick={this.timeButtonOnClick}
                data-time-spec="Minute"
              />
            </li>
            <li>
              <TimeSelectorNumberButton
                display=":45"
                dataValue="45"
                currentValue={minute}
                onClick={this.timeButtonOnClick}
                data-time-spec="Minute"
              />
            </li>
          </ul>
        </div>
      </div>
    );
  }
}

TimeSelector.propTypes = {
  start: PropTypes.string,
  stop: PropTypes.string,
  timezone: PropTypes.string,
  date: PropTypes.string,
  formCallback: PropTypes.func.isRequired,
};

export default TimeSelector;
